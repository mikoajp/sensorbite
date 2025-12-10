/* Copyright (c) 2024 Sensorbite. All rights reserved. */
package com.sensorbite.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "sensors")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Sensor {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @NotBlank(message = "Sensor type is required")
  @Column(nullable = false)
  private String type;

  @Column(length = 500)
  private String description;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "room_id", nullable = false)
  private Room room;

  @OneToMany(mappedBy = "sensor", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<SensorReading> readings = new ArrayList<>();

  public void addReading(SensorReading reading) {
    readings.add(reading);
    reading.setSensor(this);
  }

  public void removeReading(SensorReading reading) {
    readings.remove(reading);
    reading.setSensor(null);
  }
}
