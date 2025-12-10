/* Copyright (c) 2024 Sensorbite. All rights reserved. */
package com.sensorbite.service;

import com.sensorbite.dto.RoomDTO;
import com.sensorbite.entity.Floor;
import com.sensorbite.entity.Room;
import com.sensorbite.exception.ResourceAlreadyExistsException;
import com.sensorbite.exception.ResourceNotFoundException;
import com.sensorbite.repository.FloorRepository;
import com.sensorbite.repository.RoomRepository;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RoomService {

  private final RoomRepository roomRepository;
  private final FloorRepository floorRepository;

  @Transactional(readOnly = true)
  public List<RoomDTO> getAllRooms() {
    return roomRepository.findAll().stream().map(this::convertToDTO).collect(Collectors.toList());
  }

  @Transactional(readOnly = true)
  public List<RoomDTO> getRoomsByFloorId(Long floorId) {
    if (!floorRepository.existsById(floorId)) {
      throw new ResourceNotFoundException("Floor not found with id: " + floorId);
    }
    return roomRepository.findByFloorId(floorId).stream()
        .map(this::convertToDTO)
        .collect(Collectors.toList());
  }

  @Transactional(readOnly = true)
  public RoomDTO getRoomById(Long id) {
    Room room =
        roomRepository
            .findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Room not found with id: " + id));
    return convertToDTO(room);
  }

  @Transactional
  public RoomDTO createRoom(RoomDTO roomDTO) {
    Floor floor =
        floorRepository
            .findById(roomDTO.getFloorId())
            .orElseThrow(
                () ->
                    new ResourceNotFoundException(
                        "Floor not found with id: " + roomDTO.getFloorId()));

    if (roomRepository.existsByFloorIdAndRoomNumber(
        roomDTO.getFloorId(), roomDTO.getRoomNumber())) {
      throw new ResourceAlreadyExistsException(
          "Room already exists with number "
              + roomDTO.getRoomNumber()
              + " on floor "
              + roomDTO.getFloorId());
    }

    Room room = new Room();
    room.setRoomNumber(roomDTO.getRoomNumber());
    room.setDescription(roomDTO.getDescription());
    room.setFloor(floor);

    Room savedRoom = roomRepository.save(room);
    return convertToDTO(savedRoom);
  }

  @Transactional
  public RoomDTO updateRoom(Long id, RoomDTO roomDTO) {
    Room room =
        roomRepository
            .findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Room not found with id: " + id));

    Floor floor =
        floorRepository
            .findById(roomDTO.getFloorId())
            .orElseThrow(
                () ->
                    new ResourceNotFoundException(
                        "Floor not found with id: " + roomDTO.getFloorId()));

    // Check if room number already exists for a different room on the same floor
    if (!room.getRoomNumber().equals(roomDTO.getRoomNumber())
        && roomRepository.existsByFloorIdAndRoomNumber(
            roomDTO.getFloorId(), roomDTO.getRoomNumber())) {
      throw new ResourceAlreadyExistsException(
          "Room already exists with number "
              + roomDTO.getRoomNumber()
              + " on floor "
              + roomDTO.getFloorId());
    }

    room.setRoomNumber(roomDTO.getRoomNumber());
    room.setDescription(roomDTO.getDescription());
    room.setFloor(floor);

    Room updatedRoom = roomRepository.save(room);
    return convertToDTO(updatedRoom);
  }

  @Transactional
  public void deleteRoom(Long id) {
    if (!roomRepository.existsById(id)) {
      throw new ResourceNotFoundException("Room not found with id: " + id);
    }
    roomRepository.deleteById(id);
  }

  private RoomDTO convertToDTO(Room room) {
    return new RoomDTO(
        room.getId(), room.getRoomNumber(), room.getDescription(), room.getFloor().getId());
  }
}
