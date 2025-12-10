/* Copyright (c) 2024 Sensorbite. All rights reserved. */
package com.sensorbite.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BuildingDTO {
  private Long id;

  @NotBlank(message = "Building name is required")
  private String name;

  private String address;
}
