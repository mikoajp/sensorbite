/* Copyright (c) 2024 Sensorbite. All rights reserved. */
package com.sensorbite.evacuation.domain;

import java.time.LocalDateTime;
import java.util.List;
import lombok.Builder;
import lombok.Data;
import org.locationtech.jts.geom.LineString;

/**
 * Result of an evacuation route calculation. Contains the route geometry, metrics, and metadata.
 */
@Data
@Builder
public class RouteResult {

  /** The geometric representation of the route */
  private LineString geometry;

  /** Total distance of the route in meters */
  private double distanceMeters;

  /** Estimated time to traverse the route in minutes */
  private long estimatedTimeMinutes;

  /** Number of hazard zones avoided by this route */
  private int avoidedHazards;

  /** Safety score (0-100, higher is safer) */
  private double safetyScore;

  /** List of waypoint coordinates along the route */
  private List<double[]> waypoints;

  /** Timestamp when the route was calculated */
  private LocalDateTime calculatedAt;

  /** Time taken to calculate the route in milliseconds */
  private long calculationTimeMs;

  /** Algorithm used for route calculation */
  private String algorithm;

  /** Additional notes or warnings about the route */
  private String notes;
}
