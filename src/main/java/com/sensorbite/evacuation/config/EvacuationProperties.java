/* Copyright (c) 2024 Sensorbite. All rights reserved. */
package com.sensorbite.evacuation.config;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

/** Configuration properties for the evacuation module. */
@Component
@ConfigurationProperties(prefix = "sensorbite.evacuation")
@Data
@Validated
public class EvacuationProperties {

  /** Properties related to route calculation logic. */
  private RouteCalculation routeCalculation = new RouteCalculation();

  /** Properties related to penalty multipliers for hazards. */
  private Penalty penalty = new Penalty();

  /** Properties related to safety score calculation. */
  private SafetyScore safetyScore = new SafetyScore();

  /** Properties for generating route notes. */
  private Notes notes = new Notes();

  @Data
  public static class RouteCalculation {
    /** Approximate conversion factor from decimal degrees to meters at mid-latitudes. */
    @DecimalMin("100000.0")
    private double degreesToMetersFactor = 111320.0;

    /** Assumed average walking speed in kilometers per hour. */
    @DecimalMin("1.0")
    private double walkingSpeedKmh = 5.0;
  }

  @Data
  public static class Penalty {
    /**
     * The severity level at or above which a hazard is considered 'severe' and a higher penalty is
     * applied.
     */
    @Min(1)
    private int severeHazardThreshold = 4;

    /** Penalty multiplier for edges that intersect a standard hazard zone. */
    @DecimalMin("2.0")
    private double hazardMultiplier = 100.0;

    /** Penalty multiplier for edges that intersect a severe hazard zone. */
    @DecimalMin("100.0")
    private double severeHazardMultiplier = 1000.0;
  }

  @Data
  public static class SafetyScore {
    /** The threshold score at or above which a route is considered 'high' safety. */
    @Min(1)
    private double highSafetyThreshold = 80.0;

    /** The threshold score at or above which a route is considered 'medium' safety. */
    @Min(1)
    private double mediumSafetyThreshold = 50.0;

    /** Base score component for routes that intersect a hazard. */
    @DecimalMin("0.0")
    private double intersectionBaseScore = 30.0;

    /** Penalty multiplier based on severity for intersecting routes. */
    @DecimalMin("1.0")
    private double intersectionSeverityMultiplier = 5.0;

    /** Normalization factor for distance-based scoring. */
    @DecimalMin("1000.0")
    private double distanceNormalizationFactor = 10000.0;

    /** Base score for routes that do not intersect any hazards. */
    @DecimalMin("0.0")
    private double nonIntersectionBaseScore = 50.0;

    /** Bonus score component based on normalized distance for non-intersecting routes. */
    @DecimalMin("0.0")
    private double nonIntersectionDistanceBonus = 50.0;
  }

  @Data
  public static class Notes {
    /** The distance in meters above which a route is considered 'long'. */
    @Min(1)
    private double longRouteThresholdMeters = 5000.0;
  }
}
