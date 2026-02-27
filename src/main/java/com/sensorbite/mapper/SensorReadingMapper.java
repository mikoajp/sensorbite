/* Copyright (c) 2024 Sensorbite. All rights reserved. */
package com.sensorbite.mapper;

import com.sensorbite.dto.SensorReadingDTO;
import com.sensorbite.entity.SensorReading;
import org.springframework.stereotype.Component;

@Component
public class SensorReadingMapper {

  public SensorReadingDTO toDTO(SensorReading reading) {
    if (reading == null) {
      return null;
    }
    return new SensorReadingDTO(
        reading.getId(), reading.getValue(), reading.getTimestamp(), reading.getSensor().getId());
  }
}
