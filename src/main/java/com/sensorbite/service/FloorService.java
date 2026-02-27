/* Copyright (c) 2024 Sensorbite. All rights reserved. */
package com.sensorbite.service;

import com.sensorbite.dto.FloorDTO;
import com.sensorbite.entity.Building;
import com.sensorbite.entity.Floor;
import com.sensorbite.exception.ResourceAlreadyExistsException;
import com.sensorbite.exception.ResourceNotFoundException;
import com.sensorbite.mapper.FloorMapper;
import com.sensorbite.repository.BuildingRepository;
import com.sensorbite.repository.FloorRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FloorService {

  private final FloorRepository floorRepository;
  private final BuildingRepository buildingRepository;
  private final FloorMapper floorMapper;

  @Transactional(readOnly = true)
  public Page<FloorDTO> getAllFloors(Pageable pageable) {
    return floorRepository.findAll(pageable).map(floorMapper::toDTO);
  }

  @Transactional(readOnly = true)
  public List<FloorDTO> getFloorsByBuildingId(Long buildingId) {
    // Check if building exists by trying to fetch it (single query)
    buildingRepository
        .findById(buildingId)
        .orElseThrow(
            () -> new ResourceNotFoundException("Building not found with id: " + buildingId));
    return floorRepository.findByBuildingId(buildingId).stream().map(floorMapper::toDTO).toList();
  }

  @Transactional(readOnly = true)
  public FloorDTO getFloorById(Long id) {
    Floor floor =
        floorRepository
            .findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Floor not found with id: " + id));
    return floorMapper.toDTO(floor);
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
    return floorMapper.toDTO(savedFloor);
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

    // No need to call save() - JPA dirty checking will update the entity
    return floorMapper.toDTO(floor);
  }

  @Transactional
  public void deleteFloor(Long id) {
    Floor floor =
        floorRepository
            .findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Floor not found with id: " + id));
    floorRepository.delete(floor);
  }
}
