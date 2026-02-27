/* Copyright (c) 2024 Sensorbite. All rights reserved. */
package com.sensorbite.service;

import com.sensorbite.dto.SensorDTO;
import com.sensorbite.entity.Room;
import com.sensorbite.entity.Sensor;
import com.sensorbite.exception.ResourceNotFoundException;
import com.sensorbite.mapper.SensorMapper;
import com.sensorbite.repository.RoomRepository;
import com.sensorbite.repository.SensorRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SensorService {

  private final SensorRepository sensorRepository;
  private final RoomRepository roomRepository;
  private final SensorMapper sensorMapper;

  @Transactional(readOnly = true)
  public Page<SensorDTO> getAllSensors(Pageable pageable) {
    return sensorRepository.findAll(pageable).map(sensorMapper::toDTO);
  }

  @Transactional(readOnly = true)
  public List<SensorDTO> getSensorsByRoomId(Long roomId) {
    // Check if room exists by trying to fetch it (single query)
    roomRepository
        .findById(roomId)
        .orElseThrow(() -> new ResourceNotFoundException("Room not found with id: " + roomId));
    return sensorRepository.findByRoomId(roomId).stream().map(sensorMapper::toDTO).toList();
  }

  @Transactional(readOnly = true)
  public SensorDTO getSensorById(Long id) {
    Sensor sensor =
        sensorRepository
            .findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Sensor not found with id: " + id));
    return sensorMapper.toDTO(sensor);
  }

  @Transactional
  public SensorDTO createSensor(SensorDTO sensorDTO) {
    Room room =
        roomRepository
            .findById(sensorDTO.getRoomId())
            .orElseThrow(
                () ->
                    new ResourceNotFoundException(
                        "Room not found with id: " + sensorDTO.getRoomId()));

    Sensor sensor = new Sensor();
    sensor.setType(sensorDTO.getType());
    sensor.setDescription(sensorDTO.getDescription());
    sensor.setRoom(room);

    Sensor savedSensor = sensorRepository.save(sensor);
    return sensorMapper.toDTO(savedSensor);
  }

  @Transactional
  public SensorDTO updateSensor(Long id, SensorDTO sensorDTO) {
    Sensor sensor =
        sensorRepository
            .findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Sensor not found with id: " + id));

    Room room =
        roomRepository
            .findById(sensorDTO.getRoomId())
            .orElseThrow(
                () ->
                    new ResourceNotFoundException(
                        "Room not found with id: " + sensorDTO.getRoomId()));

    sensor.setType(sensorDTO.getType());
    sensor.setDescription(sensorDTO.getDescription());
    sensor.setRoom(room);

    // No need to call save() - JPA dirty checking will update the entity
    return sensorMapper.toDTO(sensor);
  }

  @Transactional
  public void deleteSensor(Long id) {
    Sensor sensor =
        sensorRepository
            .findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Sensor not found with id: " + id));
    sensorRepository.delete(sensor);
  }
}
