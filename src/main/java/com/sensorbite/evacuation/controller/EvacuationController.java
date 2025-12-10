/* Copyright (c) 2024 Sensorbite. All rights reserved. */
package com.sensorbite.evacuation.controller;

import com.sensorbite.evacuation.domain.HazardZone;
import com.sensorbite.evacuation.domain.RoadNode;
import com.sensorbite.evacuation.domain.RouteResult;
import com.sensorbite.evacuation.dto.RouteRequest;
import com.sensorbite.evacuation.dto.RouteResponse;
import com.sensorbite.evacuation.exception.InvalidCoordinatesException;
import com.sensorbite.evacuation.exception.NoRouteFoundException;
import com.sensorbite.evacuation.service.EvacuationRoutingService;
import com.sensorbite.evacuation.service.RoadNetworkService;
import com.sensorbite.evacuation.service.SentinelHubClient;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/** REST Controller for evacuation route calculation endpoints. */
@RestController
@RequestMapping("/api/evac")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Evacuation Routes", description = "Evacuation route calculation API")
public class EvacuationController {

  private final EvacuationRoutingService routingService;
  private final RoadNetworkService roadNetworkService;
  private final SentinelHubClient sentinelHubClient;

  private static final double MIN_LAT = -90.0;
  private static final double MAX_LAT = 90.0;
  private static final double MIN_LON = -180.0;
  private static final double MAX_LON = 180.0;

  /**
   * Calculate evacuation route between two points.
   *
   * @param request Route calculation request with start/end coordinates
   * @return GeoJSON response with route geometry and metadata
   */
  @GetMapping("/route")
  @Operation(
      summary = "Calculate evacuation route",
      description =
          "Calculates the safest evacuation route between two geographic points, avoiding hazard zones")
  public ResponseEntity<RouteResponse> calculateRoute(@Valid @ModelAttribute RouteRequest request) {

    log.info(
        "Received route request: start=[{}, {}], end=[{}, {}]",
        request.getStartLat(),
        request.getStartLon(),
        request.getEndLat(),
        request.getEndLon());

    try {
      // Validate coordinates
      validateCoordinates(request);

      // Create geometry points
      GeometryFactory geometryFactory = new GeometryFactory();
      Point start =
          geometryFactory.createPoint(new Coordinate(request.getStartLon(), request.getStartLat()));
      Point end =
          geometryFactory.createPoint(new Coordinate(request.getEndLon(), request.getEndLat()));

      // Load road network (using sample data for now)
      Graph<RoadNode, DefaultWeightedEdge> roadNetwork = loadRoadNetwork(request.getNetworkName());

      // Calculate bounding box for hazard zone query
      double minLon = Math.min(request.getStartLon(), request.getEndLon());
      double maxLon = Math.max(request.getStartLon(), request.getEndLon());
      double minLat = Math.min(request.getStartLat(), request.getEndLat());
      double maxLat = Math.max(request.getStartLat(), request.getEndLat());

      // Add buffer to bounding box (10% on each side)
      double lonBuffer = (maxLon - minLon) * 0.1;
      double latBuffer = (maxLat - minLat) * 0.1;
      minLon -= lonBuffer;
      maxLon += lonBuffer;
      minLat -= latBuffer;
      maxLat += latBuffer;

      // Get hazard zones from Sentinel Hub
      List<HazardZone> hazardZones =
          request.getIncludeHazards()
              ? loadHazardZones(minLon, minLat, maxLon, maxLat)
              : Collections.emptyList();

      // Calculate route
      RouteResult result = routingService.findSafestRoute(start, end, roadNetwork, hazardZones);

      // Convert to response DTO
      RouteResponse response = convertToResponse(result);

      log.info(
          "Route calculated successfully: {} km, {} minutes",
          response.getFeatures().get(0).getProperties().getDistanceKm(),
          response.getFeatures().get(0).getProperties().getEstimatedTimeMinutes());

      return ResponseEntity.ok(response);

    } catch (InvalidCoordinatesException e) {
      log.warn("Invalid coordinates: {}", e.getMessage());
      throw e;
    } catch (NoRouteFoundException e) {
      log.warn("No route found: {}", e.getMessage());
      throw e;
    } catch (Exception e) {
      log.error("Error calculating route", e);
      throw new RuntimeException("Failed to calculate route: " + e.getMessage(), e);
    }
  }

  /** Health check endpoint for evacuation service. */
  @GetMapping("/health")
  @Operation(summary = "Health check", description = "Check if evacuation service is operational")
  public ResponseEntity<String> health() {
    return ResponseEntity.ok("Evacuation service is operational");
  }

  /** Validates that coordinates are within valid ranges. */
  private void validateCoordinates(RouteRequest request) {
    if (request.getStartLat() < MIN_LAT || request.getStartLat() > MAX_LAT) {
      throw new InvalidCoordinatesException(
          "Start latitude must be between " + MIN_LAT + " and " + MAX_LAT);
    }

    if (request.getStartLon() < MIN_LON || request.getStartLon() > MAX_LON) {
      throw new InvalidCoordinatesException(
          "Start longitude must be between " + MIN_LON + " and " + MAX_LON);
    }

    if (request.getEndLat() < MIN_LAT || request.getEndLat() > MAX_LAT) {
      throw new InvalidCoordinatesException(
          "End latitude must be between " + MIN_LAT + " and " + MAX_LAT);
    }

    if (request.getEndLon() < MIN_LON || request.getEndLon() > MAX_LON) {
      throw new InvalidCoordinatesException(
          "End longitude must be between " + MIN_LON + " and " + MAX_LON);
    }

    // Check if start and end are different
    if (request.getStartLat().equals(request.getEndLat())
        && request.getStartLon().equals(request.getEndLon())) {
      throw new InvalidCoordinatesException("Start and end points must be different");
    }
  }

  /** Loads road network from file or uses sample data. */
  private Graph<RoadNode, DefaultWeightedEdge> loadRoadNetwork(String networkName) {
    try {
      // Try to load from resources
      String resourcePath =
          networkName != null ? "data/networks/" + networkName : "data/sample_network.geojson";

      ClassPathResource resource = new ClassPathResource(resourcePath);

      if (resource.exists()) {
        try (InputStream inputStream = resource.getInputStream()) {
          return roadNetworkService.loadRoadNetwork(inputStream);
        }
      } else {
        log.warn("Network file not found: {}, using sample data", resourcePath);
        return createSampleNetwork();
      }

    } catch (Exception e) {
      log.warn("Failed to load network file, using sample data: {}", e.getMessage());
      return createSampleNetwork();
    }
  }

  /** Creates a simple sample road network for testing. */
  private Graph<RoadNode, DefaultWeightedEdge> createSampleNetwork() {
    log.info("Creating sample road network");

    Graph<RoadNode, DefaultWeightedEdge> graph =
        new org.jgrapht.graph.SimpleWeightedGraph<>(DefaultWeightedEdge.class);

    GeometryFactory factory = new GeometryFactory();

    // Create a simple grid network
    RoadNode[][] nodes = new RoadNode[5][5];

    for (int i = 0; i < 5; i++) {
      for (int j = 0; j < 5; j++) {
        double lat = 52.0 + (i * 0.01); // Around Warsaw coordinates
        double lon = 21.0 + (j * 0.01);
        Point point = factory.createPoint(new Coordinate(lon, lat));
        nodes[i][j] = new RoadNode("node_" + i + "_" + j, point);
        graph.addVertex(nodes[i][j]);
      }
    }

    // Connect nodes horizontally and vertically
    for (int i = 0; i < 5; i++) {
      for (int j = 0; j < 5; j++) {
        // Horizontal connection
        if (j < 4) {
          DefaultWeightedEdge edge = graph.addEdge(nodes[i][j], nodes[i][j + 1]);
          if (edge != null) {
            graph.setEdgeWeight(edge, 1000.0); // ~1km
          }
        }

        // Vertical connection
        if (i < 4) {
          DefaultWeightedEdge edge = graph.addEdge(nodes[i][j], nodes[i + 1][j]);
          if (edge != null) {
            graph.setEdgeWeight(edge, 1000.0); // ~1km
          }
        }
      }
    }

    log.info(
        "Sample network created: {} nodes, {} edges",
        graph.vertexSet().size(),
        graph.edgeSet().size());

    return graph;
  }

  /** Loads hazard zones from Sentinel Hub API. */
  private List<HazardZone> loadHazardZones(
      double minLon, double minLat, double maxLon, double maxLat) {
    log.debug("Loading hazard zones from Sentinel Hub");
    try {
      return sentinelHubClient.getFloodZones(minLon, minLat, maxLon, maxLat);
    } catch (Exception e) {
      log.error("Failed to load hazard zones, proceeding without hazard data", e);
      return new ArrayList<>();
    }
  }

  /** Converts RouteResult to GeoJSON response format. */
  private RouteResponse convertToResponse(RouteResult result) {
    // Convert geometry to coordinate array
    List<double[]> coordinates = result.getWaypoints();

    // Determine hazard level based on safety score
    String hazardLevel;
    if (result.getSafetyScore() >= 80) {
      hazardLevel = "low";
    } else if (result.getSafetyScore() >= 50) {
      hazardLevel = "medium";
    } else {
      hazardLevel = "high";
    }

    // Build geometry
    RouteResponse.Geometry geometry =
        RouteResponse.Geometry.builder().type("LineString").coordinates(coordinates).build();

    // Build properties
    RouteResponse.Properties properties =
        RouteResponse.Properties.builder()
            .distanceKm(result.getDistanceMeters() / 1000.0)
            .estimatedTimeMinutes(result.getEstimatedTimeMinutes())
            .hazardLevel(hazardLevel)
            .avoidedZones(result.getAvoidedHazards())
            .safetyScore(result.getSafetyScore())
            .notes(result.getNotes())
            .build();

    // Build feature
    RouteResponse.Feature feature =
        RouteResponse.Feature.builder()
            .type("Feature")
            .geometry(geometry)
            .properties(properties)
            .build();

    // Build metadata
    RouteResponse.Metadata metadata =
        RouteResponse.Metadata.builder()
            .calculationTimeMs(result.getCalculationTimeMs())
            .algorithm(result.getAlgorithm())
            .timestamp(result.getCalculatedAt())
            .totalWaypoints(coordinates.size())
            .build();

    // Build response
    return RouteResponse.builder()
        .type("FeatureCollection")
        .features(List.of(feature))
        .metadata(metadata)
        .build();
  }

  /** Exception handler for invalid coordinates. */
  @ExceptionHandler(InvalidCoordinatesException.class)
  public ResponseEntity<String> handleInvalidCoordinates(InvalidCoordinatesException e) {
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
  }

  /** Exception handler for no route found. */
  @ExceptionHandler(NoRouteFoundException.class)
  public ResponseEntity<String> handleNoRouteFound(NoRouteFoundException e) {
    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
  }
}
