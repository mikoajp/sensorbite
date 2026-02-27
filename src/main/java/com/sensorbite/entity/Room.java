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
@Table(
    name = "rooms",
    uniqueConstraints = {@UniqueConstraint(columnNames = {"floor_id", "room_number"})})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Room {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @NotBlank(message = "Room number is required")
  @Column(nullable = false)
  private String roomNumber;

  @Column(length = 500)
  private String description;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "floor_id", nullable = false)
  private Floor floor;

  @OneToMany(mappedBy = "room", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<Sensor> sensors = new ArrayList<>();

  public void addSensor(Sensor sensor) {
    sensors.add(sensor);
    sensor.setRoom(this);
  }

  public void removeSensor(Sensor sensor) {
    sensors.remove(sensor);
    sensor.setRoom(null);
  }
}
