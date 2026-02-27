/* Copyright (c) 2024 Sensorbite. All rights reserved. */
package com.sensorbite.controller;

import com.sensorbite.dto.SensorReadingDTO;
import com.sensorbite.service.SensorReadingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/sensor-readings")
@RequiredArgsConstructor
@Tag(name = "Sensor Readings", description = "Sensor reading management APIs")
public class SensorReadingController {

  private final SensorReadingService sensorReadingService;

  @GetMapping
  @Operation(summary = "Get all sensor readings")
  public ResponseEntity<Page<SensorReadingDTO>> getAllReadings(
      @PageableDefault(size = 20, sort = "timestamp", direction = Sort.Direction.DESC)
          Pageable pageable) {
    return ResponseEntity.ok(sensorReadingService.getAllReadings(pageable));
  }

  @GetMapping("/{id}")
  @Operation(summary = "Get sensor reading by ID")
  public ResponseEntity<SensorReadingDTO> getReadingById(@PathVariable Long id) {
    return ResponseEntity.ok(sensorReadingService.getReadingById(id));
  }

  @GetMapping("/sensor/{sensorId}")
  @Operation(summary = "Get all readings for a sensor")
  public ResponseEntity<List<SensorReadingDTO>> getReadingsBySensorId(@PathVariable Long sensorId) {
    return ResponseEntity.ok(sensorReadingService.getReadingsBySensorId(sensorId));
  }

  @GetMapping("/sensor/{sensorId}/range")
  @Operation(summary = "Get readings for a sensor within a time range")
  public ResponseEntity<List<SensorReadingDTO>> getReadingsBySensorIdAndTimeRange(
      @PathVariable Long sensorId,
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {
    return ResponseEntity.ok(
        sensorReadingService.getReadingsBySensorIdAndTimeRange(sensorId, startTime, endTime));
  }

  @PostMapping
  @Operation(summary = "Create a new sensor reading")
  public ResponseEntity<SensorReadingDTO> createReading(
      @Valid @RequestBody SensorReadingDTO readingDTO) {
    SensorReadingDTO created = sensorReadingService.createReading(readingDTO);
    return ResponseEntity.status(HttpStatus.CREATED).body(created);
  }

  @DeleteMapping("/{id}")
  @Operation(summary = "Delete a sensor reading")
  public ResponseEntity<Void> deleteReading(@PathVariable Long id) {
    sensorReadingService.deleteReading(id);
    return ResponseEntity.noContent().build();
  }
}
