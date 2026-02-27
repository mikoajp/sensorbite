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
@Table(name = "buildings")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Building {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @NotBlank(message = "Building name is required")
  @Column(nullable = false, unique = true)
  private String name;

  @Column(length = 500)
  private String address;

  @OneToMany(mappedBy = "building", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<Floor> floors = new ArrayList<>();

  public void addFloor(Floor floor) {
    floors.add(floor);
    floor.setBuilding(this);
  }

  public void removeFloor(Floor floor) {
    floors.remove(floor);
    floor.setBuilding(null);
  }
}
