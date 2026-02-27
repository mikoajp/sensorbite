/* Copyright (c) 2024 Sensorbite. All rights reserved. */
package com.sensorbite.controller;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sensorbite.dto.BuildingDTO;
import com.sensorbite.entity.Building;
import com.sensorbite.repository.BuildingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class BuildingControllerIntegrationTest {

  @Autowired private MockMvc mockMvc;

  @Autowired private BuildingRepository buildingRepository;

  @Autowired private ObjectMapper objectMapper;

  private Building building;

  @BeforeEach
  void setUp() {
    buildingRepository.deleteAll();

    building = new Building();
    building.setName("Test Building");
    building.setAddress("123 Test Street");
    buildingRepository.save(building);
  }

  @Test
  void getAllBuildings_ShouldReturnAllBuildings() throws Exception {
    mockMvc
        .perform(get("/api/buildings"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content", hasSize(1)))
        .andExpect(jsonPath("$.content[0].name", is("Test Building")));
  }

  @Test
  void getBuildingById_WhenExists_ShouldReturnBuilding() throws Exception {
    mockMvc
        .perform(get("/api/buildings/{id}", building.getId()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.name", is("Test Building")))
        .andExpect(jsonPath("$.address", is("123 Test Street")));
  }

  @Test
  void getBuildingById_WhenNotExists_ShouldReturn404() throws Exception {
    mockMvc.perform(get("/api/buildings/{id}", 999L)).andExpect(status().isNotFound());
  }

  @Test
  void createBuilding_WithValidData_ShouldCreateBuilding() throws Exception {
    BuildingDTO newBuilding = new BuildingDTO();
    newBuilding.setName("New Building");
    newBuilding.setAddress("456 New Street");

    mockMvc
        .perform(
            post("/api/buildings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newBuilding)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.name", is("New Building")))
        .andExpect(jsonPath("$.address", is("456 New Street")));
  }

  @Test
  void createBuilding_WithDuplicateName_ShouldReturn409() throws Exception {
    BuildingDTO duplicateBuilding = new BuildingDTO();
    duplicateBuilding.setName("Test Building");
    duplicateBuilding.setAddress("789 Duplicate Street");

    mockMvc
        .perform(
            post("/api/buildings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(duplicateBuilding)))
        .andExpect(status().isConflict());
  }

  @Test
  void createBuilding_WithInvalidData_ShouldReturn400() throws Exception {
    BuildingDTO invalidBuilding = new BuildingDTO();
    invalidBuilding.setName(""); // Empty name

    mockMvc
        .perform(
            post("/api/buildings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidBuilding)))
        .andExpect(status().isBadRequest());
  }

  @Test
  void updateBuilding_WhenExists_ShouldUpdateBuilding() throws Exception {
    BuildingDTO updatedBuilding = new BuildingDTO();
    updatedBuilding.setName("Updated Building");
    updatedBuilding.setAddress("Updated Address");

    mockMvc
        .perform(
            put("/api/buildings/{id}", building.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updatedBuilding)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.name", is("Updated Building")))
        .andExpect(jsonPath("$.address", is("Updated Address")));
  }

  @Test
  void updateBuilding_WhenNotExists_ShouldReturn404() throws Exception {
    BuildingDTO updatedBuilding = new BuildingDTO();
    updatedBuilding.setName("Updated Building");
    updatedBuilding.setAddress("Updated Address");

    mockMvc
        .perform(
            put("/api/buildings/{id}", 999L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updatedBuilding)))
        .andExpect(status().isNotFound());
  }

  @Test
  void deleteBuilding_WhenExists_ShouldDeleteBuilding() throws Exception {
    mockMvc
        .perform(delete("/api/buildings/{id}", building.getId()))
        .andExpect(status().isNoContent());

    mockMvc.perform(get("/api/buildings/{id}", building.getId())).andExpect(status().isNotFound());
  }

  @Test
  void deleteBuilding_WhenNotExists_ShouldReturn404() throws Exception {
    mockMvc.perform(delete("/api/buildings/{id}", 999L)).andExpect(status().isNotFound());
  }
}
