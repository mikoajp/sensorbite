/* Copyright (c) 2024 Sensorbite. All rights reserved. */
package com.sensorbite.repository;

import com.sensorbite.entity.Floor;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FloorRepository extends JpaRepository<Floor, Long> {
  List<Floor> findByBuildingId(Long buildingId);

  Optional<Floor> findByBuildingIdAndLevel(Long buildingId, Integer level);

  boolean existsByBuildingIdAndLevel(Long buildingId, Integer level);
}
