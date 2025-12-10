/* Copyright (c) 2024 Sensorbite. All rights reserved. */
package com.sensorbite.repository;

import com.sensorbite.entity.Room;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RoomRepository extends JpaRepository<Room, Long> {
  List<Room> findByFloorId(Long floorId);

  Optional<Room> findByFloorIdAndRoomNumber(Long floorId, String roomNumber);

  boolean existsByFloorIdAndRoomNumber(Long floorId, String roomNumber);
}
