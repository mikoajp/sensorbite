/* Copyright (c) 2024 Sensorbite. All rights reserved. */
package com.sensorbite.service;

import com.sensorbite.dto.SensorReadingDTO;
import com.sensorbite.entity.Sensor;
import com.sensorbite.entity.SensorReading;
import com.sensorbite.exception.ResourceNotFoundException;
import com.sensorbite.repository.SensorReadingRepository;
import com.sensorbite.repository.SensorRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SensorReadingService {

  private final SensorReadingRepository sensorReadingRepository;
  private final SensorRepository sensorRepository;

  @Transactional(readOnly = true)
  public List<SensorReadingDTO> getAllReadings() {
    return sensorReadingRepository.findAll().stream()
        .map(this::convertToDTO)
        .collect(Collectors.toList());
  }

  @Transactional(readOnly = true)
  public List<SensorReadingDTO> getReadingsBySensorId(Long sensorId) {
    if (!sensorRepository.existsById(sensorId)) {
      throw new ResourceNotFoundException("Sensor not found with id: " + sensorId);
    }
    return sensorReadingRepository.findBySensorId(sensorId).stream()
        .map(this::convertToDTO)
        .collect(Collectors.toList());
  }

  @Transactional(readOnly = true)
  public List<SensorReadingDTO> getReadingsBySensorIdAndTimeRange(
      Long sensorId, LocalDateTime startTime, LocalDateTime endTime) {
    if (!sensorRepository.existsById(sensorId)) {
      throw new ResourceNotFoundException("Sensor not found with id: " + sensorId);
    }
    return sensorReadingRepository
        .findBySensorIdAndTimestampBetween(sensorId, startTime, endTime)
        .stream()
        .map(this::convertToDTO)
        .collect(Collectors.toList());
  }

  @Transactional(readOnly = true)
  public SensorReadingDTO getReadingById(Long id) {
    SensorReading reading =
        sensorReadingRepository
            .findById(id)
            .orElseThrow(
                () -> new ResourceNotFoundException("Sensor reading not found with id: " + id));
    return convertToDTO(reading);
  }

  @Transactional
  public SensorReadingDTO createReading(SensorReadingDTO readingDTO) {
    Sensor sensor =
        sensorRepository
            .findById(readingDTO.getSensorId())
            .orElseThrow(
                () ->
                    new ResourceNotFoundException(
                        "Sensor not found with id: " + readingDTO.getSensorId()));

    SensorReading reading = new SensorReading();
    reading.setValue(readingDTO.getValue());
    reading.setTimestamp(
        readingDTO.getTimestamp() != null ? readingDTO.getTimestamp() : LocalDateTime.now());
    reading.setSensor(sensor);

    SensorReading savedReading = sensorReadingRepository.save(reading);
    return convertToDTO(savedReading);
  }

  @Transactional
  public void deleteReading(Long id) {
    if (!sensorReadingRepository.existsById(id)) {
      throw new ResourceNotFoundException("Sensor reading not found with id: " + id);
    }
    sensorReadingRepository.deleteById(id);
  }

  private SensorReadingDTO convertToDTO(SensorReading reading) {
    return new SensorReadingDTO(
        reading.getId(), reading.getValue(), reading.getTimestamp(), reading.getSensor().getId());
  }
}
