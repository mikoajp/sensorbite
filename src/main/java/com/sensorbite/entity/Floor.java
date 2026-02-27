/* Copyright (c) 2024 Sensorbite. All rights reserved. */
package com.sensorbite.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(
    name = "floors",
    uniqueConstraints = {@UniqueConstraint(columnNames = {"building_id", "level"})})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Floor {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @NotNull(message = "Floor level is required")
  @Column(nullable = false)
  private Integer level;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "building_id", nullable = false)
  private Building building;

  @OneToMany(mappedBy = "floor", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<Room> rooms = new ArrayList<>();

  public void addRoom(Room room) {
    rooms.add(room);
    room.setFloor(this);
  }

  public void removeRoom(Room room) {
    rooms.remove(room);
    room.setFloor(null);
  }
}
