/* Copyright (c) 2024 Sensorbite. All rights reserved. */
package com.sensorbite.evacuation.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Response DTO for evacuation route in GeoJSON format. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RouteResponse {

  /** GeoJSON type (always "FeatureCollection") */
  private String type = "FeatureCollection";

  /** List of GeoJSON features (route geometry) */
  private List<Feature> features;

  /** Metadata about the route calculation */
  private Metadata metadata;

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class Feature {
    @Builder.Default private String type = "Feature";
    private Geometry geometry;
    private Properties properties;
  }

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class Geometry {
    @Builder.Default private String type = "LineString";
    private List<double[]> coordinates;
  }

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class Properties {

    @JsonProperty("distance_km")
    private Double distanceKm;

    @JsonProperty("estimated_time_minutes")
    private Long estimatedTimeMinutes;

    @JsonProperty("hazard_level")
    private String hazardLevel;

    @JsonProperty("avoided_zones")
    private Integer avoidedZones;

    @JsonProperty("safety_score")
    private Double safetyScore;

    private String notes;
  }

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class Metadata {

    @JsonProperty("calculation_time_ms")
    private Long calculationTimeMs;

    private String algorithm;

    private LocalDateTime timestamp;

    @JsonProperty("total_waypoints")
    private Integer totalWaypoints;
  }
}
