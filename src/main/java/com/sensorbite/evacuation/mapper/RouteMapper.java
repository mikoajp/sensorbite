/* Copyright (c) 2024 Sensorbite. All rights reserved. */
package com.sensorbite.evacuation.mapper;

import com.sensorbite.evacuation.config.EvacuationProperties;
import com.sensorbite.evacuation.domain.RouteResult;
import com.sensorbite.evacuation.dto.RouteResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Maps domain objects to DTOs for the evacuation feature. */
@Component
@RequiredArgsConstructor
public class RouteMapper {

  private final EvacuationProperties properties;

  /**
   * Converts RouteResult to GeoJSON response format.
   *
   * @param result The result from the routing service.
   * @return A {@link RouteResponse} DTO.
   */
  public RouteResponse toResponse(RouteResult result) {
    // Convert geometry to coordinate array
    List<double[]> coordinates = result.getWaypoints();

    // Determine hazard level based on safety score
    String hazardLevel = determineHazardLevel(result.getSafetyScore());

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

  private String determineHazardLevel(double safetyScore) {
    EvacuationProperties.SafetyScore scoreProps = properties.getSafetyScore();
    if (safetyScore >= scoreProps.getHighSafetyThreshold()) {
      return "low";
    } else if (safetyScore >= scoreProps.getMediumSafetyThreshold()) {
      return "medium";
    } else {
      return "high";
    }
  }
}
