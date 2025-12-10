/* Copyright (c) 2024 Sensorbite. All rights reserved. */
package com.sensorbite.service;

import com.sensorbite.dto.FloorDTO;
import com.sensorbite.entity.Building;
import com.sensorbite.entity.Floor;
import com.sensorbite.exception.ResourceAlreadyExistsException;
import com.sensorbite.exception.ResourceNotFoundException;
import com.sensorbite.repository.BuildingRepository;
import com.sensorbite.repository.FloorRepository;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FloorService {

  private final FloorRepository floorRepository;
  private final BuildingRepository buildingRepository;

  @Transactional(readOnly = true)
  public List<FloorDTO> getAllFloors() {
    return floorRepository.findAll().stream().map(this::convertToDTO).collect(Collectors.toList());
  }

  @Transactional(readOnly = true)
  public List<FloorDTO> getFloorsByBuildingId(Long buildingId) {
    if (!buildingRepository.existsById(buildingId)) {
      throw new ResourceNotFoundException("Building not found with id: " + buildingId);
    }
    return floorRepository.findByBuildingId(buildingId).stream()
        .map(this::convertToDTO)
        .collect(Collectors.toList());
  }

  @Transactional(readOnly = true)
  public FloorDTO getFloorById(Long id) {
    Floor floor =
        floorRepository
            .findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Floor not found with id: " + id));
    return convertToDTO(floor);
  }

  @Transactional
  public FloorDTO createFloor(FloorDTO floorDTO) {
    Building building =
        buildingRepository
            .findById(floorDTO.getBuildingId())
            .orElseThrow(
                () ->
                    new ResourceNotFoundException(
                        "Building not found with id: " + floorDTO.getBuildingId()));

    if (floorRepository.existsByBuildingIdAndLevel(floorDTO.getBuildingId(), floorDTO.getLevel())) {
      throw new ResourceAlreadyExistsException(
          "Floor already exists with level "
              + floorDTO.getLevel()
              + " in building "
              + floorDTO.getBuildingId());
    }

    Floor floor = new Floor();
    floor.setLevel(floorDTO.getLevel());
    floor.setBuilding(building);

    Floor savedFloor = floorRepository.save(floor);
    return convertToDTO(savedFloor);
  }

  @Transactional
  public FloorDTO updateFloor(Long id, FloorDTO floorDTO) {
    Floor floor =
        floorRepository
            .findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Floor not found with id: " + id));

    Building building =
        buildingRepository
            .findById(floorDTO.getBuildingId())
            .orElseThrow(
                () ->
                    new ResourceNotFoundException(
                        "Building not found with id: " + floorDTO.getBuildingId()));

    // Check if level already exists for a different floor in the same building
    if (!floor.getLevel().equals(floorDTO.getLevel())
        && floorRepository.existsByBuildingIdAndLevel(
            floorDTO.getBuildingId(), floorDTO.getLevel())) {
      throw new ResourceAlreadyExistsException(
          "Floor already exists with level "
              + floorDTO.getLevel()
              + " in building "
              + floorDTO.getBuildingId());
    }

    floor.setLevel(floorDTO.getLevel());
    floor.setBuilding(building);

    Floor updatedFloor = floorRepository.save(floor);
    return convertToDTO(updatedFloor);
  }

  @Transactional
  public void deleteFloor(Long id) {
    if (!floorRepository.existsById(id)) {
      throw new ResourceNotFoundException("Floor not found with id: " + id);
    }
    floorRepository.deleteById(id);
  }

  private FloorDTO convertToDTO(Floor floor) {
    return new FloorDTO(floor.getId(), floor.getLevel(), floor.getBuilding().getId());
  }
}
