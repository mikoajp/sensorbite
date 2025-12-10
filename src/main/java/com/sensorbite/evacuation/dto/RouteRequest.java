/* Copyright (c) 2024 Sensorbite. All rights reserved. */
package com.sensorbite.evacuation.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Request DTO for evacuation route calculation. */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RouteRequest {

  /** Starting point latitude */
  @NotNull(message = "Start latitude is required")
  private Double startLat;

  /** Starting point longitude */
  @NotNull(message = "Start longitude is required")
  private Double startLon;

  /** Ending point latitude */
  @NotNull(message = "End latitude is required")
  private Double endLat;

  /** Ending point longitude */
  @NotNull(message = "End longitude is required")
  private Double endLon;

  /** Optional: Name of the road network file to use If not provided, uses default network */
  private String networkName;

  /** Optional: Include hazard zones in calculation (default: true) */
  private Boolean includeHazards = true;
}
