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
import com.sensorbite.repository.RoomRepository;
import com.sensorbite.repository.SensorRepository;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SensorServiceTest {

  @Mock private SensorRepository sensorRepository;

  @Mock private RoomRepository roomRepository;

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
    when(sensorRepository.findAll()).thenReturn(Arrays.asList(sensor));

    // Act
    List<SensorDTO> result = sensorService.getAllSensors();

    // Assert
    assertEquals(1, result.size());
    assertEquals("TEMPERATURE", result.get(0).getType());
    verify(sensorRepository, times(1)).findAll();
  }

  @Test
  void getSensorById_WhenExists_ShouldReturnSensor() {
    // Arrange
    when(sensorRepository.findById(1L)).thenReturn(Optional.of(sensor));

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
    when(roomRepository.existsById(1L)).thenReturn(true);
    when(sensorRepository.findByRoomId(1L)).thenReturn(Arrays.asList(sensor));

    // Act
    List<SensorDTO> result = sensorService.getSensorsByRoomId(1L);

    // Assert
    assertEquals(1, result.size());
    verify(sensorRepository, times(1)).findByRoomId(1L);
  }

  @Test
  void getSensorsByRoomId_WhenRoomNotExists_ShouldThrowException() {
    // Arrange
    when(roomRepository.existsById(1L)).thenReturn(false);

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
    when(sensorRepository.save(any(Sensor.class))).thenReturn(sensor);

    sensorDTO.setType("HUMIDITY");

    // Act
    SensorDTO result = sensorService.updateSensor(1L, sensorDTO);

    // Assert
    assertNotNull(result);
    verify(sensorRepository, times(1)).save(any(Sensor.class));
  }

  @Test
  void deleteSensor_WhenExists_ShouldDeleteSensor() {
    // Arrange
    when(sensorRepository.existsById(1L)).thenReturn(true);
    doNothing().when(sensorRepository).deleteById(1L);

    // Act
    sensorService.deleteSensor(1L);

    // Assert
    verify(sensorRepository, times(1)).deleteById(1L);
  }

  @Test
  void deleteSensor_WhenNotExists_ShouldThrowException() {
    // Arrange
    when(sensorRepository.existsById(1L)).thenReturn(false);

    // Act & Assert
    assertThrows(
        ResourceNotFoundException.class,
        () -> {
          sensorService.deleteSensor(1L);
        });
  }
}
