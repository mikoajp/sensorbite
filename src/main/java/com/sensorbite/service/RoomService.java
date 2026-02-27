/* Copyright (c) 2024 Sensorbite. All rights reserved. */
package com.sensorbite.service;

import com.sensorbite.dto.RoomDTO;
import com.sensorbite.entity.Floor;
import com.sensorbite.entity.Room;
import com.sensorbite.exception.ResourceAlreadyExistsException;
import com.sensorbite.exception.ResourceNotFoundException;
import com.sensorbite.mapper.RoomMapper;
import com.sensorbite.repository.FloorRepository;
import com.sensorbite.repository.RoomRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RoomService {

  private final RoomRepository roomRepository;
  private final FloorRepository floorRepository;
  private final RoomMapper roomMapper;

  @Transactional(readOnly = true)
  public Page<RoomDTO> getAllRooms(Pageable pageable) {
    return roomRepository.findAll(pageable).map(roomMapper::toDTO);
  }

  @Transactional(readOnly = true)
  public List<RoomDTO> getRoomsByFloorId(Long floorId) {
    // Check if floor exists by trying to fetch it (single query)
    floorRepository
        .findById(floorId)
        .orElseThrow(() -> new ResourceNotFoundException("Floor not found with id: " + floorId));
    return roomRepository.findByFloorId(floorId).stream().map(roomMapper::toDTO).toList();
  }

  @Transactional(readOnly = true)
  public RoomDTO getRoomById(Long id) {
    Room room =
        roomRepository
            .findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Room not found with id: " + id));
    return roomMapper.toDTO(room);
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
    return roomMapper.toDTO(savedRoom);
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

    // No need to call save() - JPA dirty checking will update the entity
    return roomMapper.toDTO(room);
  }

  @Transactional
  public void deleteRoom(Long id) {
    Room room =
        roomRepository
            .findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Room not found with id: " + id));
    roomRepository.delete(room);
  }
}
