/* Copyright (c) 2024 Sensorbite. All rights reserved. */
package com.sensorbite.service;

import com.sensorbite.dto.SensorReadingDTO;
import com.sensorbite.entity.Sensor;
import com.sensorbite.entity.SensorReading;
import com.sensorbite.exception.ResourceNotFoundException;
import com.sensorbite.mapper.SensorReadingMapper;
import com.sensorbite.repository.SensorReadingRepository;
import com.sensorbite.repository.SensorRepository;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SensorReadingService {

  private final SensorReadingRepository sensorReadingRepository;
  private final SensorRepository sensorRepository;
  private final SensorReadingMapper sensorReadingMapper;

  @Transactional(readOnly = true)
  public Page<SensorReadingDTO> getAllReadings(Pageable pageable) {
    return sensorReadingRepository.findAll(pageable).map(sensorReadingMapper::toDTO);
  }

  @Transactional(readOnly = true)
  public List<SensorReadingDTO> getReadingsBySensorId(Long sensorId) {
    // Check if sensor exists by trying to fetch it (single query)
    sensorRepository
        .findById(sensorId)
        .orElseThrow(() -> new ResourceNotFoundException("Sensor not found with id: " + sensorId));
    return sensorReadingRepository.findBySensorId(sensorId).stream()
        .map(sensorReadingMapper::toDTO)
        .toList();
  }

  @Transactional(readOnly = true)
  public List<SensorReadingDTO> getReadingsBySensorIdAndTimeRange(
      Long sensorId, LocalDateTime startTime, LocalDateTime endTime) {
    // Check if sensor exists by trying to fetch it (single query)
    sensorRepository
        .findById(sensorId)
        .orElseThrow(() -> new ResourceNotFoundException("Sensor not found with id: " + sensorId));
    return sensorReadingRepository
        .findBySensorIdAndTimestampBetween(sensorId, startTime, endTime)
        .stream()
        .map(sensorReadingMapper::toDTO)
        .toList();
  }

  @Transactional(readOnly = true)
  public SensorReadingDTO getReadingById(Long id) {
    SensorReading reading =
        sensorReadingRepository
            .findById(id)
            .orElseThrow(
                () -> new ResourceNotFoundException("Sensor reading not found with id: " + id));
    return sensorReadingMapper.toDTO(reading);
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
    return sensorReadingMapper.toDTO(savedReading);
  }

  @Transactional
  public void deleteReading(Long id) {
    SensorReading reading =
        sensorReadingRepository
            .findById(id)
            .orElseThrow(
                () -> new ResourceNotFoundException("Sensor reading not found with id: " + id));
    sensorReadingRepository.delete(reading);
  }
}
