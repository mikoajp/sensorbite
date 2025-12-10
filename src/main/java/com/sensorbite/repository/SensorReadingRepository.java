/* Copyright (c) 2024 Sensorbite. All rights reserved. */
package com.sensorbite.repository;

import com.sensorbite.entity.SensorReading;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface SensorReadingRepository extends JpaRepository<SensorReading, Long> {
  List<SensorReading> findBySensorId(Long sensorId);

  List<SensorReading> findBySensorIdAndTimestampBetween(
      Long sensorId, LocalDateTime startTime, LocalDateTime endTime);

  @Query(
      "SELECT sr FROM SensorReading sr WHERE sr.sensor.id = :sensorId ORDER BY sr.timestamp DESC")
  List<SensorReading> findLatestBySensorId(Long sensorId);
}
