/* Copyright (c) 2024 Sensorbite. All rights reserved. */
package com.sensorbite.mapper;

import com.sensorbite.dto.FloorDTO;
import com.sensorbite.entity.Floor;
import org.springframework.stereotype.Component;

@Component
public class FloorMapper {

  public FloorDTO toDTO(Floor floor) {
    if (floor == null) {
      return null;
    }
    return new FloorDTO(floor.getId(), floor.getLevel(), floor.getBuilding().getId());
  }
}
