/* Copyright (c) 2024 Sensorbite. All rights reserved. */
package com.sensorbite.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.sensorbite.dto.BuildingDTO;
import com.sensorbite.entity.Building;
import com.sensorbite.exception.ResourceAlreadyExistsException;
import com.sensorbite.exception.ResourceNotFoundException;
import com.sensorbite.mapper.BuildingMapper;
import com.sensorbite.repository.BuildingRepository;
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
class BuildingServiceTest {

  @Mock private BuildingRepository buildingRepository;

  @Mock private BuildingMapper buildingMapper;

  @InjectMocks private BuildingService buildingService;

  private Building building;
  private BuildingDTO buildingDTO;

  @BeforeEach
  void setUp() {
    building = new Building();
    building.setId(1L);
    building.setName("Test Building");
    building.setAddress("123 Test Street");

    buildingDTO = new BuildingDTO();
    buildingDTO.setName("Test Building");
    buildingDTO.setAddress("123 Test Street");
  }

  @Test
  void getAllBuildings_ShouldReturnAllBuildings() {
    // Arrange
    Pageable pageable = PageRequest.of(0, 10);
    Page<Building> buildingPage = new PageImpl<>(Arrays.asList(building));
    when(buildingRepository.findAll(pageable)).thenReturn(buildingPage);
    when(buildingMapper.toDTO(building)).thenReturn(buildingDTO);

    // Act
    Page<BuildingDTO> result = buildingService.getAllBuildings(pageable);

    // Assert
    assertEquals(1, result.getTotalElements());
    assertEquals("Test Building", result.getContent().get(0).getName());
    verify(buildingRepository, times(1)).findAll(pageable);
  }

  @Test
  void getBuildingById_WhenExists_ShouldReturnBuilding() {
    // Arrange
    when(buildingRepository.findById(1L)).thenReturn(Optional.of(building));
    when(buildingMapper.toDTO(building)).thenReturn(buildingDTO);

    // Act
    BuildingDTO result = buildingService.getBuildingById(1L);

    // Assert
    assertNotNull(result);
    assertEquals("Test Building", result.getName());
    verify(buildingRepository, times(1)).findById(1L);
  }

  @Test
  void getBuildingById_WhenNotExists_ShouldThrowException() {
    // Arrange
    when(buildingRepository.findById(1L)).thenReturn(Optional.empty());

    // Act & Assert
    assertThrows(
        ResourceNotFoundException.class,
        () -> {
          buildingService.getBuildingById(1L);
        });
  }

  @Test
  void createBuilding_WhenNameNotExists_ShouldCreateBuilding() {
    // Arrange
    when(buildingRepository.existsByName("Test Building")).thenReturn(false);
    when(buildingRepository.save(any(Building.class))).thenReturn(building);
    when(buildingMapper.toDTO(building)).thenReturn(buildingDTO);

    // Act
    BuildingDTO result = buildingService.createBuilding(buildingDTO);

    // Assert
    assertNotNull(result);
    assertEquals("Test Building", result.getName());
    verify(buildingRepository, times(1)).save(any(Building.class));
  }

  @Test
  void createBuilding_WhenNameExists_ShouldThrowException() {
    // Arrange
    when(buildingRepository.existsByName("Test Building")).thenReturn(true);

    // Act & Assert
    assertThrows(
        ResourceAlreadyExistsException.class,
        () -> {
          buildingService.createBuilding(buildingDTO);
        });
  }

  @Test
  void updateBuilding_WhenExists_ShouldUpdateBuilding() {
    // Arrange
    when(buildingRepository.findById(1L)).thenReturn(Optional.of(building));
    when(buildingRepository.existsByName("Updated Building")).thenReturn(false);
    when(buildingMapper.toDTO(building)).thenReturn(buildingDTO);

    buildingDTO.setName("Updated Building");

    // Act
    BuildingDTO result = buildingService.updateBuilding(1L, buildingDTO);

    // Assert
    assertNotNull(result);
    // No longer expect save() to be called due to dirty checking
    verify(buildingRepository, never()).save(any(Building.class));
  }

  @Test
  void deleteBuilding_WhenExists_ShouldDeleteBuilding() {
    // Arrange
    when(buildingRepository.findById(1L)).thenReturn(Optional.of(building));
    doNothing().when(buildingRepository).delete(building);

    // Act
    buildingService.deleteBuilding(1L);

    // Assert
    verify(buildingRepository, times(1)).delete(building);
  }

  @Test
  void deleteBuilding_WhenNotExists_ShouldThrowException() {
    // Arrange
    when(buildingRepository.findById(1L)).thenReturn(Optional.empty());

    // Act & Assert
    assertThrows(
        ResourceNotFoundException.class,
        () -> {
          buildingService.deleteBuilding(1L);
        });
  }
}
