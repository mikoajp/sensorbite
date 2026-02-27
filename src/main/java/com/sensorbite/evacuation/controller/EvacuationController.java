/* Copyright (c) 2024 Sensorbite. All rights reserved. */
package com.sensorbite.evacuation.controller;

import com.sensorbite.evacuation.domain.RouteResult;
import com.sensorbite.evacuation.dto.RouteRequest;
import com.sensorbite.evacuation.dto.RouteResponse;
import com.sensorbite.evacuation.mapper.RouteMapper;
import com.sensorbite.evacuation.service.EvacuationOrchestrationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/** REST Controller for evacuation route calculation endpoints. */
@RestController
@RequestMapping("/api/evac")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Evacuation Routes", description = "Evacuation route calculation API")
public class EvacuationController {

  private final EvacuationOrchestrationService orchestrationService;
  private final RouteMapper routeMapper;

  /**
   * Calculate evacuation route between two points.
   *
   * @param request Route calculation request with start/end coordinates
   * @return GeoJSON response with route geometry and metadata
   */
  @GetMapping("/route")
  @Operation(
      summary = "Calculate evacuation route",
      description =
          "Calculates the safest evacuation route between two geographic points, avoiding hazard zones")
  public ResponseEntity<RouteResponse> calculateRoute(@Valid @ModelAttribute RouteRequest request) {

    log.info(
        "Received route request: start=[{}, {}], end=[{}, {}]",
        request.getStartLat(),
        request.getStartLon(),
        request.getEndLat(),
        request.getEndLon());

    // Delegate all logic to the orchestration service
    RouteResult result = orchestrationService.calculateEvacuationRoute(request);

    // Convert the domain result to a DTO response
    RouteResponse response = routeMapper.toResponse(result);

    // Safe access with null check
    if (response != null
        && response.getFeatures() != null
        && !response.getFeatures().isEmpty()
        && response.getFeatures().get(0).getProperties() != null) {
      log.info(
          "Route calculated successfully: {} km, {} minutes",
          response.getFeatures().get(0).getProperties().getDistanceKm(),
          response.getFeatures().get(0).getProperties().getEstimatedTimeMinutes());
    } else {
      log.warn("Route calculated but response structure is incomplete");
    }

    return ResponseEntity.ok(response);
  }

  /** 
   * Health check endpoint for evacuation service. 
   * Note: Consider using Spring Boot Actuator /actuator/health for production health checks.
   * This endpoint provides a basic operational status.
   */
  @GetMapping("/health")
  @Operation(summary = "Health check", description = "Check if evacuation service is operational")
  public ResponseEntity<String> health() {
    // Return operational status
    // The fact that this endpoint is reachable confirms the service is running
    // For more sophisticated health checks, use Spring Boot Actuator
    return ResponseEntity.ok("Evacuation service is operational");
  }
}
