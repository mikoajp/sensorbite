/* Copyright (c) 2024 Sensorbite. All rights reserved. */
package com.sensorbite.controller;

import com.sensorbite.dto.BuildingDTO;
import com.sensorbite.service.BuildingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/buildings")
@RequiredArgsConstructor
@Tag(name = "Buildings", description = "Building management APIs")
public class BuildingController {

  private final BuildingService buildingService;

  @GetMapping
  @Operation(summary = "Get all buildings")
  public ResponseEntity<List<BuildingDTO>> getAllBuildings() {
    return ResponseEntity.ok(buildingService.getAllBuildings());
  }

  @GetMapping("/{id}")
  @Operation(summary = "Get building by ID")
  public ResponseEntity<BuildingDTO> getBuildingById(@PathVariable Long id) {
    return ResponseEntity.ok(buildingService.getBuildingById(id));
  }

  @PostMapping
  @Operation(summary = "Create a new building")
  public ResponseEntity<BuildingDTO> createBuilding(@Valid @RequestBody BuildingDTO buildingDTO) {
    BuildingDTO created = buildingService.createBuilding(buildingDTO);
    return ResponseEntity.status(HttpStatus.CREATED).body(created);
  }

  @PutMapping("/{id}")
  @Operation(summary = "Update an existing building")
  public ResponseEntity<BuildingDTO> updateBuilding(
      @PathVariable Long id, @Valid @RequestBody BuildingDTO buildingDTO) {
    return ResponseEntity.ok(buildingService.updateBuilding(id, buildingDTO));
  }

  @DeleteMapping("/{id}")
  @Operation(summary = "Delete a building")
  public ResponseEntity<Void> deleteBuilding(@PathVariable Long id) {
    buildingService.deleteBuilding(id);
    return ResponseEntity.noContent().build();
  }
}
