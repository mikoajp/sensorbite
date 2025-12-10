/* Copyright (c) 2024 Sensorbite. All rights reserved. */
package com.sensorbite.service;

import com.sensorbite.dto.SensorDTO;
import com.sensorbite.entity.Room;
import com.sensorbite.entity.Sensor;
import com.sensorbite.exception.ResourceNotFoundException;
import com.sensorbite.repository.RoomRepository;
import com.sensorbite.repository.SensorRepository;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SensorService {

  private final SensorRepository sensorRepository;
  private final RoomRepository roomRepository;

  @Transactional(readOnly = true)
  public List<SensorDTO> getAllSensors() {
    return sensorRepository.findAll().stream().map(this::convertToDTO).collect(Collectors.toList());
  }

  @Transactional(readOnly = true)
  public List<SensorDTO> getSensorsByRoomId(Long roomId) {
    if (!roomRepository.existsById(roomId)) {
      throw new ResourceNotFoundException("Room not found with id: " + roomId);
    }
    return sensorRepository.findByRoomId(roomId).stream()
        .map(this::convertToDTO)
        .collect(Collectors.toList());
  }

  @Transactional(readOnly = true)
  public SensorDTO getSensorById(Long id) {
    Sensor sensor =
        sensorRepository
            .findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Sensor not found with id: " + id));
    return convertToDTO(sensor);
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
    return convertToDTO(savedSensor);
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

    Sensor updatedSensor = sensorRepository.save(sensor);
    return convertToDTO(updatedSensor);
  }

  @Transactional
  public void deleteSensor(Long id) {
    if (!sensorRepository.existsById(id)) {
      throw new ResourceNotFoundException("Sensor not found with id: " + id);
    }
    sensorRepository.deleteById(id);
  }

  private SensorDTO convertToDTO(Sensor sensor) {
    return new SensorDTO(
        sensor.getId(), sensor.getType(), sensor.getDescription(), sensor.getRoom().getId());
  }
}
