/* Copyright (c) 2024 Sensorbite. All rights reserved. */
package com.sensorbite.evacuation.controller;

import com.sensorbite.evacuation.domain.RouteResult;
import com.sensorbite.evacuation.dto.RouteRequest;
import com.sensorbite.evacuation.dto.RouteResponse;
import com.sensorbite.evacuation.exception.InvalidCoordinatesException;
import com.sensorbite.evacuation.exception.NoRouteFoundException;
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

    try {
      // Delegate all logic to the orchestration service
      RouteResult result = orchestrationService.calculateEvacuationRoute(request);

      // Convert the domain result to a DTO response
      RouteResponse response = routeMapper.toResponse(result);

      log.info(
          "Route calculated successfully: {} km, {} minutes",
          response.getFeatures().get(0).getProperties().getDistanceKm(),
          response.getFeatures().get(0).getProperties().getEstimatedTimeMinutes());

      return ResponseEntity.ok(response);

    } catch (InvalidCoordinatesException | NoRouteFoundException e) {
      log.warn("Failed to calculate route: {}", e.getMessage());
      throw e; // Re-throw to be caught by @ExceptionHandler
    } catch (Exception e) {
      log.error("An unexpected error occurred while calculating the route", e);
      // To avoid exposing internal details, we throw a generic runtime exception.
      // GlobalExceptionHandler will handle this and return a 500 error.
      throw new RuntimeException("An unexpected error occurred while processing your request.", e);
    }
  }

  /** Health check endpoint for evacuation service. */
  @GetMapping("/health")
  @Operation(summary = "Health check", description = "Check if evacuation service is operational")
  public ResponseEntity<String> health() {
    // Basic check to see if the main service bean is available
    boolean isOperational = orchestrationService != null;
    if (isOperational) {
      return ResponseEntity.ok("Evacuation service is operational");
    } else {
      return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
          .body("Evacuation service is not available");
    }
  }

  /** Exception handler for invalid coordinates. */
  @ExceptionHandler(InvalidCoordinatesException.class)
  public ResponseEntity<String> handleInvalidCoordinates(InvalidCoordinatesException e) {
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
  }

  /** Exception handler for no route found. */
  @ExceptionHandler(NoRouteFoundException.class)
  public ResponseEntity<String> handleNoRouteFound(NoRouteFoundException e) {
    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
  }
}
