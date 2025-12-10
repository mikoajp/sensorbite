/* Copyright (c) 2024 Sensorbite. All rights reserved. */
package com.sensorbite.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(
    name = "sensor_readings",
    indexes = {@Index(name = "idx_sensor_timestamp", columnList = "sensor_id,timestamp")})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SensorReading {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @NotNull(message = "Reading value is required")
  @Column(name = "reading_value", nullable = false)
  private Double value;

  @NotNull(message = "Timestamp is required")
  @Column(nullable = false)
  private LocalDateTime timestamp;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "sensor_id", nullable = false)
  private Sensor sensor;

  @PrePersist
  protected void onCreate() {
    if (timestamp == null) {
      timestamp = LocalDateTime.now();
    }
  }
}
