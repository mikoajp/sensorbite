/* Copyright (c) 2024 Sensorbite. All rights reserved. */
package com.sensorbite.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FloorDTO {
  private Long id;

  @NotNull(message = "Floor level is required")
  private Integer level;

  @NotNull(message = "Building ID is required")
  private Long buildingId;
}
