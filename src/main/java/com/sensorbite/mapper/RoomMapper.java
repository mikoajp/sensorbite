/* Copyright (c) 2024 Sensorbite. All rights reserved. */
package com.sensorbite.mapper;

import com.sensorbite.dto.RoomDTO;
import com.sensorbite.entity.Room;
import org.springframework.stereotype.Component;

@Component
public class RoomMapper {

  public RoomDTO toDTO(Room room) {
    if (room == null) {
      return null;
    }
    return new RoomDTO(
        room.getId(), room.getRoomNumber(), room.getDescription(), room.getFloor().getId());
  }
}
