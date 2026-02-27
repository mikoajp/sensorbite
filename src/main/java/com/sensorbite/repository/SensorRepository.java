/* Copyright (c) 2024 Sensorbite. All rights reserved. */
package com.sensorbite.repository;

import com.sensorbite.entity.Sensor;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SensorRepository extends JpaRepository<Sensor, Long> {
  List<Sensor> findByRoomId(Long roomId);

  List<Sensor> findByType(String type);
}
