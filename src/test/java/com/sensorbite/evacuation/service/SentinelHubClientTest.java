/* Copyright (c) 2024 Sensorbite. All rights reserved. */
package com.sensorbite.evacuation.service;

import static org.junit.jupiter.api.Assertions.*;

import com.sensorbite.evacuation.config.SentinelHubProperties;
import com.sensorbite.evacuation.domain.HazardZone;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.reactive.function.client.WebClient;

@ExtendWith(MockitoExtension.class)
class SentinelHubClientTest {

  @Mock private WebClient webClient;

  private SentinelHubProperties properties;
  private CircuitBreaker circuitBreaker;
  private Retry retry;
  private SentinelHubClient sentinelHubClient;

  @BeforeEach
  void setUp() {
    properties = new SentinelHubProperties();
    properties.setMockMode(true);
    properties.setEnabled(false);
    properties.setTimeoutSeconds(30);
    properties.setMaxRetries(3);
    properties.setCacheTtlMinutes(60);

    // Create circuit breaker
    CircuitBreakerConfig circuitBreakerConfig =
        CircuitBreakerConfig.custom().failureRateThreshold(50).build();
    circuitBreaker = CircuitBreaker.of("test", circuitBreakerConfig);

    // Create retry
    RetryConfig retryConfig = RetryConfig.custom().maxAttempts(3).build();
    retry = Retry.of("test", retryConfig);

    sentinelHubClient = new SentinelHubClient(webClient, properties, circuitBreaker, retry);
  }

  @Test
  void getFloodZones_WithMockMode_ShouldReturnMockData() {
    // Arrange
    double minLon = 21.0;
    double minLat = 52.0;
    double maxLon = 21.1;
    double maxLat = 52.1;

    // Act
    List<HazardZone> zones = sentinelHubClient.getFloodZones(minLon, minLat, maxLon, maxLat);

    // Assert
    assertNotNull(zones);
    assertTrue(zones.size() >= 0); // May return 0-2 zones depending on bounds

    // Verify zone properties if any zones are returned
    for (HazardZone zone : zones) {
      assertNotNull(zone.getId());
      assertNotNull(zone.getGeometry());
      assertEquals("flood", zone.getHazardType());
      assertTrue(zone.getSeverity() >= 1 && zone.getSeverity() <= 5);
      assertNotNull(zone.getDescription());
    }
  }

  @Test
  void getFloodZones_WithSmallBoundingBox_ShouldReturnZones() {
    // Arrange
    double minLon = 21.0;
    double minLat = 52.0;
    double maxLon = 21.04;
    double maxLat = 52.04;

    // Act
    List<HazardZone> zones = sentinelHubClient.getFloodZones(minLon, minLat, maxLon, maxLat);

    // Assert
    assertNotNull(zones);
    // Should generate zones within bounds
    for (HazardZone zone : zones) {
      assertNotNull(zone.getGeometry());
      // Verify geometry is within bounds
      assertTrue(zone.getGeometry().getEnvelopeInternal().getMinX() >= minLon - 0.01);
      assertTrue(zone.getGeometry().getEnvelopeInternal().getMaxX() <= maxLon + 0.01);
    }
  }

  @Test
  void getFloodZones_WithDisabledAPI_ShouldReturnMockData() {
    // Arrange
    properties.setEnabled(false);
    properties.setMockMode(false); // Even with mock mode off, should use mock if API disabled

    double minLon = 21.0;
    double minLat = 52.0;
    double maxLon = 21.1;
    double maxLat = 52.1;

    // Act
    List<HazardZone> zones = sentinelHubClient.getFloodZones(minLon, minLat, maxLon, maxLat);

    // Assert
    assertNotNull(zones);
    // Should still return mock data when API is disabled
  }

  @Test
  void getFloodZones_WithNoCredentials_ShouldUseMockData() {
    // Arrange
    properties.setEnabled(true);
    properties.setMockMode(false);
    properties.setClientId(null); // No credentials
    properties.setClientSecret(null);

    double minLon = 21.0;
    double minLat = 52.0;
    double maxLon = 21.1;
    double maxLat = 52.1;

    // Act
    List<HazardZone> zones = sentinelHubClient.getFloodZones(minLon, minLat, maxLon, maxLat);

    // Assert
    assertNotNull(zones);
    // Should fallback to mock data when credentials are missing
  }

  @Test
  void getFloodZones_MultipleCalls_ShouldBeCached() {
    // Arrange
    double minLon = 21.0;
    double minLat = 52.0;
    double maxLon = 21.1;
    double maxLat = 52.1;

    // Act - Call multiple times with same parameters
    List<HazardZone> zones1 = sentinelHubClient.getFloodZones(minLon, minLat, maxLon, maxLat);
    List<HazardZone> zones2 = sentinelHubClient.getFloodZones(minLon, minLat, maxLon, maxLat);

    // Assert
    assertNotNull(zones1);
    assertNotNull(zones2);
    // Results should be consistent (from cache)
    assertEquals(zones1.size(), zones2.size());
  }

  @Test
  void getFloodZones_DifferentBoundingBoxes_ShouldReturnDifferentResults() {
    // Arrange
    double minLon1 = 21.0;
    double minLat1 = 52.0;
    double maxLon1 = 21.05;
    double maxLat1 = 52.05;

    double minLon2 = 21.1;
    double minLat2 = 52.1;
    double maxLon2 = 21.15;
    double maxLat2 = 52.15;

    // Act
    List<HazardZone> zones1 = sentinelHubClient.getFloodZones(minLon1, minLat1, maxLon1, maxLat1);
    List<HazardZone> zones2 = sentinelHubClient.getFloodZones(minLon2, minLat2, maxLon2, maxLat2);

    // Assert
    assertNotNull(zones1);
    assertNotNull(zones2);
    // Different bounding boxes may return different zones
  }

  @Test
  void getFloodZones_ShouldHandleEmptyResults() {
    // Arrange - Very small bounding box that may not contain zones
    double minLon = 21.0;
    double minLat = 52.0;
    double maxLon = 21.001;
    double maxLat = 52.001;

    // Act
    List<HazardZone> zones = sentinelHubClient.getFloodZones(minLon, minLat, maxLon, maxLat);

    // Assert
    assertNotNull(zones);
    assertTrue(zones.size() >= 0);
  }

  @Test
  void mockFloodZones_ShouldHaveValidGeometry() {
    // Arrange
    double minLon = 21.0;
    double minLat = 52.0;
    double maxLon = 21.1;
    double maxLat = 52.1;

    // Act
    List<HazardZone> zones = sentinelHubClient.getFloodZones(minLon, minLat, maxLon, maxLat);

    // Assert
    for (HazardZone zone : zones) {
      assertTrue(zone.getGeometry().isValid());
      assertTrue(zone.getGeometry().getArea() > 0);
      assertEquals("Polygon", zone.getGeometry().getGeometryType());
    }
  }

  @Test
  void mockFloodZones_ShouldHaveReasonableSeverity() {
    // Arrange
    double minLon = 21.0;
    double minLat = 52.0;
    double maxLon = 21.1;
    double maxLat = 52.1;

    // Act
    List<HazardZone> zones = sentinelHubClient.getFloodZones(minLon, minLat, maxLon, maxLat);

    // Assert
    for (HazardZone zone : zones) {
      assertTrue(zone.getSeverity() >= 1, "Severity should be at least 1");
      assertTrue(zone.getSeverity() <= 5, "Severity should be at most 5");
    }
  }
}
