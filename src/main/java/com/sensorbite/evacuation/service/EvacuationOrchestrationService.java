/* Copyright (c) 2024 Sensorbite. All rights reserved. */
package com.sensorbite.evacuation.service;

import com.sensorbite.evacuation.domain.HazardZone;
import com.sensorbite.evacuation.domain.RouteResult;
import com.sensorbite.evacuation.dto.RouteRequest;
import com.sensorbite.evacuation.exception.InvalidCoordinatesException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.springframework.stereotype.Service;

/** Orchestrates the evacuation route calculation process. */
@Service
@RequiredArgsConstructor
@Slf4j
public class EvacuationOrchestrationService {

  private final EvacuationRoutingService routingService;
  private final SentinelHubClient sentinelHubClient;
  private final GeometryFactory geometryFactory = new GeometryFactory();

  private static final double MIN_LAT = -90.0;
  private static final double MAX_LAT = 90.0;
  private static final double MIN_LON = -180.0;
  private static final double MAX_LON = 180.0;

  /**
   * Orchestrates the calculation of the safest evacuation route.
   *
   * @param request The route request DTO.
   * @return A {@link RouteResult} domain object.
   */
  public RouteResult calculateEvacuationRoute(RouteRequest request) {
    // 1. Validate coordinates
    validateCoordinates(request);

    // 2. Create geometry points
    Point start =
        geometryFactory.createPoint(new Coordinate(request.getStartLon(), request.getStartLat()));
    Point end =
        geometryFactory.createPoint(new Coordinate(request.getEndLon(), request.getEndLat()));

    // 3. Get hazard zones
    List<HazardZone> hazardZones = getHazardZones(request);

    // 4. Calculate route, passing the network name to the routing service
    return routingService.findSafestRoute(start, end, request.getNetworkName(), hazardZones);
  }

  private List<HazardZone> getHazardZones(RouteRequest request) {
    if (!request.getIncludeHazards()) {
      return Collections.emptyList();
    }

    // Calculate bounding box for hazard zone query
    double minLon = Math.min(request.getStartLon(), request.getEndLon());
    double maxLon = Math.max(request.getStartLon(), request.getEndLon());
    double minLat = Math.min(request.getStartLat(), request.getEndLat());
    double maxLat = Math.max(request.getStartLat(), request.getEndLat());

    // Add buffer to bounding box (10% on each side)
    double lonBuffer = (maxLon - minLon) * 0.1;
    double latBuffer = (maxLat - minLat) * 0.1;

    double queryMinLon = minLon - lonBuffer;
    double queryMaxLon = maxLon + lonBuffer;
    double queryMinLat = minLat - latBuffer;
    double queryMaxLat = maxLat + latBuffer;

    log.debug("Loading hazard zones from Sentinel Hub");
    try {
      return sentinelHubClient.getFloodZones(queryMinLon, queryMinLat, queryMaxLon, queryMaxLat);
    } catch (Exception e) {
      log.error("Failed to load hazard zones, proceeding without hazard data", e);
      return new ArrayList<>();
    }
  }

  private void validateCoordinates(RouteRequest request) {
    if (request.getStartLat() < MIN_LAT || request.getStartLat() > MAX_LAT) {
      throw new InvalidCoordinatesException(
          "Start latitude must be between " + MIN_LAT + " and " + MAX_LAT);
    }

    if (request.getStartLon() < MIN_LON || request.getStartLon() > MAX_LON) {
      throw new InvalidCoordinatesException(
          "Start longitude must be between " + MIN_LON + " and " + MAX_LON);
    }

    if (request.getEndLat() < MIN_LAT || request.getEndLat() > MAX_LAT) {
      throw new InvalidCoordinatesException(
          "End latitude must be between " + MIN_LAT + " and " + MAX_LAT);
    }

    if (request.getEndLon() < MIN_LON || request.getEndLon() > MAX_LON) {
      throw new InvalidCoordinatesException(
          "End longitude must be between " + MIN_LON + " and " + MAX_LON);
    }

    // Check if start and end are different
    if (request.getStartLat().equals(request.getEndLat())
        && request.getStartLon().equals(request.getEndLon())) {
      throw new InvalidCoordinatesException("Start and end points must be different");
    }
  }
}
