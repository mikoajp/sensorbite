/* Copyright (c) 2024 Sensorbite. All rights reserved. */
package com.sensorbite.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.sensorbite.dto.SensorDTO;
import com.sensorbite.entity.Building;
import com.sensorbite.entity.Floor;
import com.sensorbite.entity.Room;
import com.sensorbite.entity.Sensor;
import com.sensorbite.exception.ResourceNotFoundException;
import com.sensorbite.mapper.SensorMapper;
import com.sensorbite.repository.RoomRepository;
import com.sensorbite.repository.SensorRepository;
import java.util.Arrays;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class SensorServiceTest {

  @Mock private SensorRepository sensorRepository;

  @Mock private RoomRepository roomRepository;

  @Mock private SensorMapper sensorMapper;

  @InjectMocks private SensorService sensorService;

  private Sensor sensor;
  private SensorDTO sensorDTO;
  private Room room;

  @BeforeEach
  void setUp() {
    Building building = new Building();
    building.setId(1L);

    Floor floor = new Floor();
    floor.setId(1L);
    floor.setLevel(1);
    floor.setBuilding(building);

    room = new Room();
    room.setId(1L);
    room.setRoomNumber("101");
    room.setFloor(floor);

    sensor = new Sensor();
    sensor.setId(1L);
    sensor.setType("TEMPERATURE");
    sensor.setDescription("Temperature sensor");
    sensor.setRoom(room);

    sensorDTO = new SensorDTO();
    sensorDTO.setType("TEMPERATURE");
    sensorDTO.setDescription("Temperature sensor");
    sensorDTO.setRoomId(1L);
  }

  @Test
  void getAllSensors_ShouldReturnAllSensors() {
    // Arrange
    Pageable pageable = PageRequest.of(0, 10);
    Page<Sensor> sensorPage = new PageImpl<>(Arrays.asList(sensor));
    when(sensorRepository.findAll(pageable)).thenReturn(sensorPage);
    when(sensorMapper.toDTO(sensor)).thenReturn(sensorDTO);

    // Act
    Page<SensorDTO> result = sensorService.getAllSensors(pageable);

    // Assert
    assertEquals(1, result.getTotalElements());
    assertEquals("TEMPERATURE", result.getContent().get(0).getType());
    verify(sensorRepository, times(1)).findAll(pageable);
  }

  @Test
  void getSensorById_WhenExists_ShouldReturnSensor() {
    // Arrange
    when(sensorRepository.findById(1L)).thenReturn(Optional.of(sensor));
    when(sensorMapper.toDTO(sensor)).thenReturn(sensorDTO);

    // Act
    SensorDTO result = sensorService.getSensorById(1L);

    // Assert
    assertNotNull(result);
    assertEquals("TEMPERATURE", result.getType());
    verify(sensorRepository, times(1)).findById(1L);
  }

  @Test
  void getSensorById_WhenNotExists_ShouldThrowException() {
    // Arrange
    when(sensorRepository.findById(1L)).thenReturn(Optional.empty());

    // Act & Assert
    assertThrows(
        ResourceNotFoundException.class,
        () -> {
          sensorService.getSensorById(1L);
        });
  }

  @Test
  void getSensorsByRoomId_WhenRoomExists_ShouldReturnSensors() {
    // Arrange
    when(roomRepository.findById(1L)).thenReturn(Optional.of(room));
    when(sensorRepository.findByRoomId(1L)).thenReturn(Arrays.asList(sensor));
    when(sensorMapper.toDTO(sensor)).thenReturn(sensorDTO);

    // Act
    var result = sensorService.getSensorsByRoomId(1L);

    // Assert
    assertEquals(1, result.size());
    verify(sensorRepository, times(1)).findByRoomId(1L);
  }

  @Test
  void getSensorsByRoomId_WhenRoomNotExists_ShouldThrowException() {
    // Arrange
    when(roomRepository.findById(1L)).thenReturn(Optional.empty());

    // Act & Assert
    assertThrows(
        ResourceNotFoundException.class,
        () -> {
          sensorService.getSensorsByRoomId(1L);
        });
  }

  @Test
  void createSensor_WhenRoomExists_ShouldCreateSensor() {
    // Arrange
    when(roomRepository.findById(1L)).thenReturn(Optional.of(room));
    when(sensorRepository.save(any(Sensor.class))).thenReturn(sensor);
    when(sensorMapper.toDTO(sensor)).thenReturn(sensorDTO);

    // Act
    SensorDTO result = sensorService.createSensor(sensorDTO);

    // Assert
    assertNotNull(result);
    assertEquals("TEMPERATURE", result.getType());
    verify(sensorRepository, times(1)).save(any(Sensor.class));
  }

  @Test
  void createSensor_WhenRoomNotExists_ShouldThrowException() {
    // Arrange
    when(roomRepository.findById(1L)).thenReturn(Optional.empty());

    // Act & Assert
    assertThrows(
        ResourceNotFoundException.class,
        () -> {
          sensorService.createSensor(sensorDTO);
        });
  }

  @Test
  void updateSensor_WhenExists_ShouldUpdateSensor() {
    // Arrange
    when(sensorRepository.findById(1L)).thenReturn(Optional.of(sensor));
    when(roomRepository.findById(1L)).thenReturn(Optional.of(room));
    when(sensorMapper.toDTO(sensor)).thenReturn(sensorDTO);

    sensorDTO.setType("HUMIDITY");

    // Act
    SensorDTO result = sensorService.updateSensor(1L, sensorDTO);

    // Assert
    assertNotNull(result);
    // No longer expect save() to be called due to dirty checking
    verify(sensorRepository, never()).save(any(Sensor.class));
  }

  @Test
  void deleteSensor_WhenExists_ShouldDeleteSensor() {
    // Arrange
    when(sensorRepository.findById(1L)).thenReturn(Optional.of(sensor));
    doNothing().when(sensorRepository).delete(sensor);

    // Act
    sensorService.deleteSensor(1L);

    // Assert
    verify(sensorRepository, times(1)).delete(sensor);
  }

  @Test
  void deleteSensor_WhenNotExists_ShouldThrowException() {
    // Arrange
    when(sensorRepository.findById(1L)).thenReturn(Optional.empty());

    // Act & Assert
    assertThrows(
        ResourceNotFoundException.class,
        () -> {
          sensorService.deleteSensor(1L);
        });
  }
}
