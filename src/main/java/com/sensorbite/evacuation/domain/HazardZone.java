/* Copyright (c) 2024 Sensorbite. All rights reserved. */
package com.sensorbite.evacuation.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.locationtech.jts.geom.Polygon;

/** Represents a hazardous area (e.g., flood zone) that should be avoided during evacuation. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HazardZone {

  /** Unique identifier for the hazard zone */
  private String id;

  /** Geographic boundary of the hazard zone */
  private Polygon geometry;

  /** Type of hazard (e.g., "flood", "fire", "chemical") */
  private String hazardType;

  /** Severity level (1-5, where 5 is most severe) */
  private int severity;

  /** Optional description of the hazard */
  private String description;
}
