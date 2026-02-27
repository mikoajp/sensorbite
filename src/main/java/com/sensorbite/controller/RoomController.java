/* Copyright (c) 2024 Sensorbite. All rights reserved. */
package com.sensorbite.controller;

import com.sensorbite.dto.RoomDTO;
import com.sensorbite.service.RoomService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/rooms")
@RequiredArgsConstructor
@Tag(name = "Rooms", description = "Room management APIs")
public class RoomController {

  private final RoomService roomService;

  @GetMapping
  @Operation(summary = "Get all rooms")
  public ResponseEntity<Page<RoomDTO>> getAllRooms(
      @PageableDefault(size = 20, sort = "id", direction = Sort.Direction.ASC) Pageable pageable) {
    return ResponseEntity.ok(roomService.getAllRooms(pageable));
  }

  @GetMapping("/{id}")
  @Operation(summary = "Get room by ID")
  public ResponseEntity<RoomDTO> getRoomById(@PathVariable Long id) {
    return ResponseEntity.ok(roomService.getRoomById(id));
  }

  @GetMapping("/floor/{floorId}")
  @Operation(summary = "Get all rooms on a floor")
  public ResponseEntity<List<RoomDTO>> getRoomsByFloorId(@PathVariable Long floorId) {
    return ResponseEntity.ok(roomService.getRoomsByFloorId(floorId));
  }

  @PostMapping
  @Operation(summary = "Create a new room")
  public ResponseEntity<RoomDTO> createRoom(@Valid @RequestBody RoomDTO roomDTO) {
    RoomDTO created = roomService.createRoom(roomDTO);
    return ResponseEntity.status(HttpStatus.CREATED).body(created);
  }

  @PutMapping("/{id}")
  @Operation(summary = "Update an existing room")
  public ResponseEntity<RoomDTO> updateRoom(
      @PathVariable Long id, @Valid @RequestBody RoomDTO roomDTO) {
    return ResponseEntity.ok(roomService.updateRoom(id, roomDTO));
  }

  @DeleteMapping("/{id}")
  @Operation(summary = "Delete a room")
  public ResponseEntity<Void> deleteRoom(@PathVariable Long id) {
    roomService.deleteRoom(id);
    return ResponseEntity.noContent().build();
  }
}
