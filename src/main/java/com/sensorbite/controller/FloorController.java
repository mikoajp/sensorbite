/* Copyright (c) 2024 Sensorbite. All rights reserved. */
package com.sensorbite.controller;

import com.sensorbite.dto.FloorDTO;
import com.sensorbite.service.FloorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/floors")
@RequiredArgsConstructor
@Tag(name = "Floors", description = "Floor management APIs")
public class FloorController {

  private final FloorService floorService;

  @GetMapping
  @Operation(summary = "Get all floors")
  public ResponseEntity<List<FloorDTO>> getAllFloors() {
    return ResponseEntity.ok(floorService.getAllFloors());
  }

  @GetMapping("/{id}")
  @Operation(summary = "Get floor by ID")
  public ResponseEntity<FloorDTO> getFloorById(@PathVariable Long id) {
    return ResponseEntity.ok(floorService.getFloorById(id));
  }

  @GetMapping("/building/{buildingId}")
  @Operation(summary = "Get all floors in a building")
  public ResponseEntity<List<FloorDTO>> getFloorsByBuildingId(@PathVariable Long buildingId) {
    return ResponseEntity.ok(floorService.getFloorsByBuildingId(buildingId));
  }

  @PostMapping
  @Operation(summary = "Create a new floor")
  public ResponseEntity<FloorDTO> createFloor(@Valid @RequestBody FloorDTO floorDTO) {
    FloorDTO created = floorService.createFloor(floorDTO);
    return ResponseEntity.status(HttpStatus.CREATED).body(created);
  }

  @PutMapping("/{id}")
  @Operation(summary = "Update an existing floor")
  public ResponseEntity<FloorDTO> updateFloor(
      @PathVariable Long id, @Valid @RequestBody FloorDTO floorDTO) {
    return ResponseEntity.ok(floorService.updateFloor(id, floorDTO));
  }

  @DeleteMapping("/{id}")
  @Operation(summary = "Delete a floor")
  public ResponseEntity<Void> deleteFloor(@PathVariable Long id) {
    floorService.deleteFloor(id);
    return ResponseEntity.noContent().build();
  }
}
