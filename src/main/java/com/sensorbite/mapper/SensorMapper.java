/* Copyright (c) 2024 Sensorbite. All rights reserved. */
package com.sensorbite.mapper;

import com.sensorbite.dto.SensorDTO;
import com.sensorbite.entity.Sensor;
import org.springframework.stereotype.Component;

@Component
public class SensorMapper {

  public SensorDTO toDTO(Sensor sensor) {
    if (sensor == null) {
      return null;
    }
    return new SensorDTO(
        sensor.getId(), sensor.getType(), sensor.getDescription(), sensor.getRoom().getId());
  }
}
