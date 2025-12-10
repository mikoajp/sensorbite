/* Copyright (c) 2024 Sensorbite. All rights reserved. */
package com.sensorbite.service;

import com.sensorbite.dto.BuildingDTO;
import com.sensorbite.entity.Building;
import com.sensorbite.exception.ResourceAlreadyExistsException;
import com.sensorbite.exception.ResourceNotFoundException;
import com.sensorbite.repository.BuildingRepository;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BuildingService {

  private final BuildingRepository buildingRepository;

  @Transactional(readOnly = true)
  public List<BuildingDTO> getAllBuildings() {
    return buildingRepository.findAll().stream()
        .map(this::convertToDTO)
        .collect(Collectors.toList());
  }

  @Transactional(readOnly = true)
  public BuildingDTO getBuildingById(Long id) {
    Building building =
        buildingRepository
            .findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Building not found with id: " + id));
    return convertToDTO(building);
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
    return convertToDTO(savedBuilding);
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

    Building updatedBuilding = buildingRepository.save(building);
    return convertToDTO(updatedBuilding);
  }

  @Transactional
  public void deleteBuilding(Long id) {
    if (!buildingRepository.existsById(id)) {
      throw new ResourceNotFoundException("Building not found with id: " + id);
    }
    buildingRepository.deleteById(id);
  }

  private BuildingDTO convertToDTO(Building building) {
    return new BuildingDTO(building.getId(), building.getName(), building.getAddress());
  }
}
