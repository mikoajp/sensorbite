/* Copyright (c) 2024 Sensorbite. All rights reserved. */
package com.sensorbite.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SensorReadingDTO {
  private Long id;

  @NotNull(message = "Reading value is required")
  private Double value;

  private LocalDateTime timestamp;

  @NotNull(message = "Sensor ID is required")
  private Long sensorId;
}
