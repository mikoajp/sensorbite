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
public class RoomDTO {
  private Long id;

  @NotBlank(message = "Room number is required")
  private String roomNumber;

  private String description;

  @NotNull(message = "Floor ID is required")
  private Long floorId;
}
