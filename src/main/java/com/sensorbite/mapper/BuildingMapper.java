/* Copyright (c) 2024 Sensorbite. All rights reserved. */
package com.sensorbite.mapper;

import com.sensorbite.dto.BuildingDTO;
import com.sensorbite.entity.Building;
import org.springframework.stereotype.Component;

@Component
public class BuildingMapper {

  public BuildingDTO toDTO(Building building) {
    if (building == null) {
      return null;
    }
    return new BuildingDTO(building.getId(), building.getName(), building.getAddress());
  }
}
