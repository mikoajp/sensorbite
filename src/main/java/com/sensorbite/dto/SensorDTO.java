/* Copyright (c) 2024 Sensorbite. All rights reserved. */
package com.sensorbite.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SensorDTO {
  private Long id;

  @NotBlank(message = "Sensor type is required")
  private String type;

  private String description;

  @NotNull(message = "Room ID is required")
  private Long roomId;
}
