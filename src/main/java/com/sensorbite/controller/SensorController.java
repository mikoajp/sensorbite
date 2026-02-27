/* Copyright (c) 2024 Sensorbite. All rights reserved. */
package com.sensorbite.controller;

import com.sensorbite.dto.SensorDTO;
import com.sensorbite.service.SensorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/sensors")
@RequiredArgsConstructor
@Tag(name = "Sensors", description = "Sensor management APIs")
public class SensorController {

  private final SensorService sensorService;

  @GetMapping
  @Operation(summary = "Get all sensors")
  public ResponseEntity<Page<SensorDTO>> getAllSensors(
      @PageableDefault(size = 20, sort = "id", direction = Sort.Direction.ASC) Pageable pageable) {
    return ResponseEntity.ok(sensorService.getAllSensors(pageable));
  }

  @GetMapping("/{id}")
  @Operation(summary = "Get sensor by ID")
  public ResponseEntity<SensorDTO> getSensorById(@PathVariable Long id) {
    return ResponseEntity.ok(sensorService.getSensorById(id));
  }

  @GetMapping("/room/{roomId}")
  @Operation(summary = "Get all sensors in a room")
  public ResponseEntity<List<SensorDTO>> getSensorsByRoomId(@PathVariable Long roomId) {
    return ResponseEntity.ok(sensorService.getSensorsByRoomId(roomId));
  }

  @PostMapping
  @Operation(summary = "Create a new sensor")
  public ResponseEntity<SensorDTO> createSensor(@Valid @RequestBody SensorDTO sensorDTO) {
    SensorDTO created = sensorService.createSensor(sensorDTO);
    return ResponseEntity.status(HttpStatus.CREATED).body(created);
  }

  @PutMapping("/{id}")
  @Operation(summary = "Update an existing sensor")
  public ResponseEntity<SensorDTO> updateSensor(
      @PathVariable Long id, @Valid @RequestBody SensorDTO sensorDTO) {
    return ResponseEntity.ok(sensorService.updateSensor(id, sensorDTO));
  }

  @DeleteMapping("/{id}")
  @Operation(summary = "Delete a sensor")
  public ResponseEntity<Void> deleteSensor(@PathVariable Long id) {
    sensorService.deleteSensor(id);
    return ResponseEntity.noContent().build();
  }
}
