/* Copyright (c) 2024 Sensorbite. All rights reserved. */
package com.sensorbite.service;

import com.sensorbite.dto.BuildingDTO;
import com.sensorbite.entity.Building;
import com.sensorbite.exception.ResourceAlreadyExistsException;
import com.sensorbite.exception.ResourceNotFoundException;
import com.sensorbite.mapper.BuildingMapper;
import com.sensorbite.repository.BuildingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BuildingService {

  private final BuildingRepository buildingRepository;
  private final BuildingMapper buildingMapper;

  @Transactional(readOnly = true)
  public Page<BuildingDTO> getAllBuildings(Pageable pageable) {
    return buildingRepository.findAll(pageable).map(buildingMapper::toDTO);
  }

  @Transactional(readOnly = true)
  public BuildingDTO getBuildingById(Long id) {
    Building building =
        buildingRepository
            .findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Building not found with id: " + id));
    return buildingMapper.toDTO(building);
  }

  @Transactional
  public BuildingDTO createBuilding(BuildingDTO buildingDTO) {
    if (buildingRepository.existsByName(buildingDTO.getName())) {
      throw new ResourceAlreadyExistsException(
          "Building already exists with name: " + buildingDTO.getName());
    }

    Building building = new Building();
    building.setName(buildingDTO.getName());
    building.setAddress(buildingDTO.getAddress());

    Building savedBuilding = buildingRepository.save(building);
    return buildingMapper.toDTO(savedBuilding);
  }

  @Transactional
  public BuildingDTO updateBuilding(Long id, BuildingDTO buildingDTO) {
    Building building =
        buildingRepository
            .findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Building not found with id: " + id));

    // Check if name already exists for a different building
    if (!building.getName().equals(buildingDTO.getName())
        && buildingRepository.existsByName(buildingDTO.getName())) {
      throw new ResourceAlreadyExistsException(
          "Building already exists with name: " + buildingDTO.getName());
    }

    building.setName(buildingDTO.getName());
    building.setAddress(buildingDTO.getAddress());

    // No need to call save() - JPA dirty checking will update the entity
    return buildingMapper.toDTO(building);
  }

  @Transactional
  public void deleteBuilding(Long id) {
    Building building =
        buildingRepository
            .findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Building not found with id: " + id));
    buildingRepository.delete(building);
  }
}
