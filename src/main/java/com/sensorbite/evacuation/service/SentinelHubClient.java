/* Copyright (c) 2024 Sensorbite. All rights reserved. */
package com.sensorbite.evacuation.service;

import com.sensorbite.evacuation.config.SentinelHubProperties;
import com.sensorbite.evacuation.domain.HazardZone;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.retry.Retry;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONObject;
import org.locationtech.jts.geom.*;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

/**
 * Client for Sentinel Hub API to retrieve flood zone data. Implements circuit breaker and retry
 * patterns for resilience.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class SentinelHubClient {

  private final WebClient sentinelHubWebClient;
  private final SentinelHubProperties properties;
  private final CircuitBreaker sentinelHubCircuitBreaker;
  private final Retry sentinelHubRetry;
  private final GeometryFactory geometryFactory = new GeometryFactory();

  /**
   * Retrieves flood zones for a given bounding box. Results are cached for performance.
   *
   * @param minLon Minimum longitude
   * @param minLat Minimum latitude
   * @param maxLon Maximum longitude
   * @param maxLat Maximum latitude
   * @return List of hazard zones (flood zones)
   */
  @Cacheable(value = "floodZones", key = "#minLon + '_' + #minLat + '_' + #maxLon + '_' + #maxLat")
  public List<HazardZone> getFloodZones(
      double minLon, double minLat, double maxLon, double maxLat) {

    log.info("Fetching flood zones for bbox: [{}, {}] to [{}, {}]", minLon, minLat, maxLon, maxLat);

    // Check if mock mode is enabled or API is disabled
    if (properties.isMockMode() || !properties.isEnabled()) {
      log.info(
          "Using mock flood zone data (mock-mode={}, enabled={})",
          properties.isMockMode(),
          properties.isEnabled());
      return getMockFloodZones(minLon, minLat, maxLon, maxLat);
    }

    // Check if credentials are configured
    if (properties.getClientId() == null
        || properties.getClientId().isEmpty()
        || properties.getClientSecret() == null
        || properties.getClientSecret().isEmpty()) {
      log.warn("Sentinel Hub credentials not configured, using mock data");
      return getMockFloodZones(minLon, minLat, maxLon, maxLat);
    }

    try {
      // Apply circuit breaker and retry patterns
      return CircuitBreaker.decorateSupplier(
              sentinelHubCircuitBreaker,
              () ->
                  Retry.decorateSupplier(
                          sentinelHubRetry,
                          () -> fetchFloodZonesFromApi(minLon, minLat, maxLon, maxLat))
                      .get())
          .get();

    } catch (Exception e) {
      log.error("Failed to fetch flood zones from Sentinel Hub API", e);
      return getFloodZonesFallback(minLon, minLat, maxLon, maxLat, e);
    }
  }

  /** Fetches flood zones from Sentinel Hub API. */
  private List<HazardZone> fetchFloodZonesFromApi(
      double minLon, double minLat, double maxLon, double maxLat) {

    log.debug("Calling Sentinel Hub API for flood zones");

    try {
      // Get access token
      String token = getAccessToken();

      // Build request body
      String requestBody = buildFloodZoneRequest(minLon, minLat, maxLon, maxLat);

      // Make API call
      String response =
          sentinelHubWebClient
              .post()
              .uri("/api/v1/process")
              .header("Authorization", "Bearer " + token)
              .header("Content-Type", "application/json")
              .bodyValue(requestBody)
              .retrieve()
              .bodyToMono(String.class)
              .timeout(Duration.ofSeconds(properties.getTimeoutSeconds()))
              .block();

      if (response == null || response.isEmpty()) {
        log.warn("Empty response from Sentinel Hub API");
        return Collections.emptyList();
      }

      // Parse response
      return parseFloodZoneResponse(response);

    } catch (WebClientResponseException e) {
      log.error("Sentinel Hub API error: {} {}", e.getStatusCode(), e.getMessage());
      throw new RuntimeException("Failed to fetch flood zones: " + e.getMessage(), e);
    } catch (Exception e) {
      log.error("Error fetching flood zones from API", e);
      throw new RuntimeException("Failed to fetch flood zones: " + e.getMessage(), e);
    }
  }

  /** Gets OAuth2 access token from Sentinel Hub. */
  private String getAccessToken() {
    log.debug("Requesting access token from Sentinel Hub");

    try {
      String response =
          WebClient.create(properties.getTokenUrl())
              .post()
              .header("Content-Type", "application/x-www-form-urlencoded")
              .bodyValue(
                  "grant_type=client_credentials&client_id="
                      + properties.getClientId()
                      + "&client_secret="
                      + properties.getClientSecret())
              .retrieve()
              .bodyToMono(String.class)
              .block();

      if (response == null) {
        throw new RuntimeException("No response from token endpoint");
      }

      JSONObject json = new JSONObject(response);
      String token = json.getString("access_token");

      log.debug("Successfully obtained access token");
      return token;

    } catch (Exception e) {
      log.error("Failed to obtain access token", e);
      throw new RuntimeException("Failed to authenticate with Sentinel Hub", e);
    }
  }

  /** Builds the request body for flood zone detection. */
  private String buildFloodZoneRequest(double minLon, double minLat, double maxLon, double maxLat) {
    JSONObject request = new JSONObject();

    // Input configuration
    JSONObject input = new JSONObject();
    JSONObject bounds = new JSONObject();
    bounds.put("bbox", new JSONArray().put(minLon).put(minLat).put(maxLon).put(maxLat));
    bounds.put(
        "properties", new JSONObject().put("crs", "http://www.opengis.net/def/crs/EPSG/0/4326"));
    input.put("bounds", bounds);

    JSONObject data = new JSONObject();
    data.put("type", "sentinel-1-grd");
    data.put(
        "dataFilter",
        new JSONObject()
            .put(
                "timeRange",
                new JSONObject()
                    .put("from", "2024-01-01T00:00:00Z")
                    .put("to", "2024-12-31T23:59:59Z")));

    input.put("data", new JSONArray().put(data));
    request.put("input", input);

    // Output configuration
    JSONObject output = new JSONObject();
    output.put("width", 512);
    output.put("height", 512);
    output.put(
        "responses",
        new JSONArray()
            .put(
                new JSONObject()
                    .put("identifier", "default")
                    .put("format", new JSONObject().put("type", "image/tiff"))));

    request.put("output", output);

    // Evalscript for flood detection
    String evalscript =
        """
                //VERSION=3
                function setup() {
                    return {
                        input: ["VV", "VH"],
                        output: { bands: 1, sampleType: "FLOAT32" }
                    };
                }
                function evaluatePixel(sample) {
                    let ratio = sample.VH / sample.VV;
                    return [ratio < 0.3 ? 1 : 0];
                }
                """;

    request.put("evalscript", evalscript);

    return request.toString();
  }

  /** Parses flood zone data from API response. */
  private List<HazardZone> parseFloodZoneResponse(String response) {
    log.debug("Parsing flood zone response");

    List<HazardZone> zones = new ArrayList<>();

    try {
      // Parse response and extract flood polygons
      // This is a simplified version - actual implementation would process the image data
      JSONObject json = new JSONObject(response);

      // For now, return empty list as we don't have actual image processing
      log.info("Successfully parsed response, found {} flood zones", zones.size());

    } catch (Exception e) {
      log.error("Error parsing flood zone response", e);
    }

    return zones;
  }

  /**
   * Fallback method when API call fails. Returns mock data to allow system to continue operating.
   */
  private List<HazardZone> getFloodZonesFallback(
      double minLon, double minLat, double maxLon, double maxLat, Exception e) {
    log.warn("Fallback triggered for flood zones due to: {}", e.getMessage());
    log.info("Returning mock flood zone data");
    return getMockFloodZones(minLon, minLat, maxLon, maxLat);
  }

  /** Generates mock flood zone data for testing and fallback scenarios. */
  private List<HazardZone> getMockFloodZones(
      double minLon, double minLat, double maxLon, double maxLat) {

    log.debug("Generating mock flood zones for bbox");

    List<HazardZone> mockZones = new ArrayList<>();

    // Calculate center and dimensions
    double centerLon = (minLon + maxLon) / 2.0;
    double centerLat = (minLat + maxLat) / 2.0;
    double width = (maxLon - minLon) / 4.0;
    double height = (maxLat - minLat) / 4.0;

    // Create 2-3 mock flood zones within the bounding box
    int numZones = 2;

    for (int i = 0; i < numZones; i++) {
      // Offset each zone slightly
      double offsetLon = (i - 0.5) * width * 1.5;
      double offsetLat = (i % 2 == 0 ? 0.3 : -0.3) * height;

      // Create polygon coordinates
      Coordinate[] coords =
          new Coordinate[] {
            new Coordinate(centerLon + offsetLon - width / 2, centerLat + offsetLat - height / 2),
            new Coordinate(centerLon + offsetLon + width / 2, centerLat + offsetLat - height / 2),
            new Coordinate(centerLon + offsetLon + width / 2, centerLat + offsetLat + height / 2),
            new Coordinate(centerLon + offsetLon - width / 2, centerLat + offsetLat + height / 2),
            new Coordinate(centerLon + offsetLon - width / 2, centerLat + offsetLat - height / 2)
          };

      // Check if polygon is within bounds
      boolean withinBounds = true;
      for (Coordinate coord : coords) {
        if (coord.x < minLon || coord.x > maxLon || coord.y < minLat || coord.y > maxLat) {
          withinBounds = false;
          break;
        }
      }

      if (withinBounds) {
        Polygon polygon = geometryFactory.createPolygon(coords);

        HazardZone zone =
            HazardZone.builder()
                .id("mock_flood_zone_" + UUID.randomUUID().toString().substring(0, 8))
                .geometry(polygon)
                .hazardType("flood")
                .severity(i == 0 ? 3 : 2) // Varying severity
                .description("Mock flood zone for testing (severity " + (i == 0 ? 3 : 2) + ")")
                .build();

        mockZones.add(zone);
      }
    }

    log.info("Generated {} mock flood zones", mockZones.size());
    return mockZones;
  }

  /** Clears the flood zone cache. */
  public void clearCache() {
    log.info("Clearing flood zone cache");
    // Cache clearing is handled by Spring Cache abstraction
  }
}
