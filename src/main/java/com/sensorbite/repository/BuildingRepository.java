/* Copyright (c) 2024 Sensorbite. All rights reserved. */
package com.sensorbite.repository;

import com.sensorbite.entity.Building;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BuildingRepository extends JpaRepository<Building, Long> {
  Optional<Building> findByName(String name);

  boolean existsByName(String name);
}
