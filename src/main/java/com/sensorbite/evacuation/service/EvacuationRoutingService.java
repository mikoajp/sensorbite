/* Copyright (c) 2024 Sensorbite. All rights reserved. */
package com.sensorbite.evacuation.service;

import com.sensorbite.evacuation.config.EvacuationProperties;
import com.sensorbite.evacuation.domain.HazardZone;
import com.sensorbite.evacuation.domain.RoadNode;
import com.sensorbite.evacuation.domain.RouteResult;
import com.sensorbite.evacuation.exception.NoRouteFoundException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jgrapht.Graph;
import org.jgrapht.GraphPath;
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;
import org.locationtech.jts.geom.*;
import org.springframework.stereotype.Service;

/**
 * Service responsible for calculating evacuation routes using modified Dijkstra's algorithm. Takes
 * into account hazard zones and calculates the safest path between two points.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class EvacuationRoutingService {

  private final RoadNetworkService roadNetworkService;
  private final EvacuationProperties properties;

  /**
   * Finds the safest evacuation route between two points, avoiding hazard zones.
   *
   * @param start Starting point
   * @param end Ending point
   * @param networkName The name of the road network to use
   * @param hazardZones List of hazard zones to avoid
   * @return RouteResult containing the calculated route and metrics
   * @throws NoRouteFoundException if no valid route can be found
   */
  public RouteResult findSafestRoute(
      Point start, Point end, String networkName, List<HazardZone> hazardZones) {

    log.info(
        "Calculating evacuation route from [{}, {}] to [{}, {}] using network '{}'",
        start.getX(),
        start.getY(),
        end.getX(),
        end.getY(),
        networkName);

    long startTime = System.currentTimeMillis();

    try {
      // 1. Get the cached road network and spatial index
      RoadNetworkService.RoadNetworkCacheEntry network =
          roadNetworkService.getRoadNetworkCacheEntry(networkName);
      Graph<RoadNode, DefaultWeightedEdge> roadNetwork = network.getGraph();

      // 2. Find nearest nodes to start/end points using the spatial index
      RoadNode startNode = roadNetworkService.findNearestNode(start, network);
      RoadNode endNode = roadNetworkService.findNearestNode(end, network);

      if (startNode == null || endNode == null) {
        throw new NoRouteFoundException("Could not find nodes near start or end points");
      }

      log.debug("Start node: {}, End node: {}", startNode.getId(), endNode.getId());

      // Check if start or end is in a hazard zone
      checkPointSafety(start, hazardZones, "start");
      checkPointSafety(end, hazardZones, "end");

      // 3. Create weighted graph with hazard penalties
      Graph<RoadNode, DefaultWeightedEdge> weightedGraph =
          createWeightedGraph(roadNetwork, hazardZones);

      // 4. Run Dijkstra algorithm
      DijkstraShortestPath<RoadNode, DefaultWeightedEdge> dijkstra =
          new DijkstraShortestPath<>(weightedGraph);

      GraphPath<RoadNode, DefaultWeightedEdge> path = dijkstra.getPath(startNode, endNode);

      if (path == null) {
        log.warn("No route found between {} and {}", startNode.getId(), endNode.getId());
        throw new NoRouteFoundException(
            "No safe route found between given points. All possible paths may be blocked.");
      }

      // 5. Build result with metrics
      long calculationTime = System.currentTimeMillis() - startTime;
      RouteResult result = buildRouteResult(path, hazardZones, calculationTime);

      log.info(
          "Route calculated successfully: {} meters, {} minutes, {} hazards avoided in {}ms",
          result.getDistanceMeters(),
          result.getEstimatedTimeMinutes(),
          result.getAvoidedHazards(),
          calculationTime);

      return result;

    } catch (NoRouteFoundException e) {
      throw e;
    } catch (Exception e) {
      log.error("Error calculating evacuation route", e);
      throw new NoRouteFoundException("Failed to calculate route: " + e.getMessage(), e);
    }
  }

  /** Creates a weighted graph where edges intersecting hazard zones have increased weight. */
  private Graph<RoadNode, DefaultWeightedEdge> createWeightedGraph(
      Graph<RoadNode, DefaultWeightedEdge> originalGraph, List<HazardZone> hazardZones) {

    log.debug("Creating weighted graph with {} hazard zones", hazardZones.size());

    Graph<RoadNode, DefaultWeightedEdge> weightedGraph =
        new SimpleWeightedGraph<>(DefaultWeightedEdge.class);

    // Add all vertices
    for (RoadNode node : originalGraph.vertexSet()) {
      weightedGraph.addVertex(node);
    }

    // Add edges with hazard-adjusted weights
    for (DefaultWeightedEdge edge : originalGraph.edgeSet()) {
      RoadNode source = originalGraph.getEdgeSource(edge);
      RoadNode target = originalGraph.getEdgeTarget(edge);

      double baseWeight = originalGraph.getEdgeWeight(edge);
      double adjustedWeight = calculateAdjustedWeight(source, target, baseWeight, hazardZones);

      DefaultWeightedEdge newEdge = weightedGraph.addEdge(source, target);
      if (newEdge != null) {
        weightedGraph.setEdgeWeight(newEdge, adjustedWeight);
      }
    }

    return weightedGraph;
  }

  /** Calculates adjusted weight for an edge based on hazard zone intersections. */
  private double calculateAdjustedWeight(
      RoadNode source, RoadNode target, double baseWeight, List<HazardZone> hazardZones) {

    if (hazardZones.isEmpty()) {
      return baseWeight;
    }

    // Create line segment between nodes
    GeometryFactory factory = new GeometryFactory();
    Coordinate[] coords =
        new Coordinate[] {
          source.getLocation().getCoordinate(), target.getLocation().getCoordinate()
        };
    LineString segment = factory.createLineString(coords);

    // Check intersection with hazard zones
    double penalty = 1.0;
    int intersectingZones = 0;

    EvacuationProperties.Penalty penaltyProps = properties.getPenalty();

    for (HazardZone zone : hazardZones) {
      if (zone.getGeometry().intersects(segment)) {
        intersectingZones++;

        // Apply penalty based on severity
        double severityMultiplier =
            zone.getSeverity() >= penaltyProps.getSevereHazardThreshold()
                ? penaltyProps.getSevereHazardMultiplier()
                : penaltyProps.getHazardMultiplier();

        penalty *= severityMultiplier;

        log.debug(
            "Edge from {} to {} intersects hazard zone {} (severity: {})",
            source.getId(),
            target.getId(),
            zone.getId(),
            zone.getSeverity());
      }
    }

    if (intersectingZones > 0) {
      log.debug(
          "Applied penalty multiplier {} to edge (intersects {} zones)",
          penalty,
          intersectingZones);
    }

    return baseWeight * penalty;
  }

  /** Builds the final route result with all metrics and metadata. */
  private RouteResult buildRouteResult(
      GraphPath<RoadNode, DefaultWeightedEdge> path,
      List<HazardZone> hazardZones,
      long calculationTimeMs) {

    // Extract nodes from path
    List<RoadNode> nodes = path.getVertexList();

    // Build route geometry
    LineString routeGeometry = createRouteGeometry(nodes);

    // The path weight is now the sum of accurate distances in meters.
    double totalDistance = path.getWeight();

    // Calculate estimated time
    long estimatedTimeMinutes =
        (long)
            ((totalDistance / 1000.0)
                / properties.getRouteCalculation().getWalkingSpeedKmh()
                * 60);

    // Count avoided hazards
    int avoidedZones = countAvoidedHazards(routeGeometry, hazardZones);

    // Calculate safety score
    double safetyScore = calculateSafetyScore(routeGeometry, hazardZones);

    // Build waypoints
    List<double[]> waypoints = buildWaypoints(nodes);

    // Create notes
    String notes = generateRouteNotes(avoidedZones, safetyScore, totalDistance);

    return RouteResult.builder()
        .geometry(routeGeometry)
        .distanceMeters(totalDistance)
        .estimatedTimeMinutes(estimatedTimeMinutes)
        .avoidedHazards(avoidedZones)
        .safetyScore(safetyScore)
        .waypoints(waypoints)
        .calculatedAt(LocalDateTime.now())
        .calculationTimeMs(calculationTimeMs)
        .algorithm("modified_dijkstra")
        .notes(notes)
        .build();
  }

  /** Creates a LineString geometry from a list of nodes. */
  private LineString createRouteGeometry(List<RoadNode> nodes) {
    GeometryFactory factory = new GeometryFactory();
    Coordinate[] coords =
        nodes.stream().map(node -> node.getLocation().getCoordinate()).toArray(Coordinate[]::new);

    return factory.createLineString(coords);
  }

  /** Counts how many hazard zones would have been encountered on a a direct path. */
  private int countAvoidedHazards(LineString route, List<HazardZone> hazardZones) {
    int count = 0;

    for (HazardZone zone : hazardZones) {
      // Check if route avoids this zone (doesn't intersect)
      if (!route.intersects(zone.getGeometry())) {
        count++;
      }
    }

    return count;
  }

  /**
   * Calculates a safety score based on distance from hazard zones. Score ranges from 0 (very
   * unsafe) to 100 (very safe).
   */
  private double calculateSafetyScore(LineString route, List<HazardZone> hazardZones) {
    if (hazardZones.isEmpty()) {
      return 100.0;
    }

    EvacuationProperties.SafetyScore scoreProps = properties.getSafetyScore();
    double minDistance = Double.MAX_VALUE;

    for (HazardZone zone : hazardZones) {
      if (route.intersects(zone.getGeometry())) {
        // Severe penalty for intersection
        return Math.max(
            0,
            scoreProps.getIntersectionBaseScore()
                - (zone.getSeverity() * scoreProps.getIntersectionSeverityMultiplier()));
      }

      double distance = route.distance(zone.getGeometry());
      minDistance = Math.min(minDistance, distance);
    }

    // Score based on minimum distance to any hazard
    // Distance in degrees, normalize to 0-100 scale
    double normalizedDistance =
        Math.min(minDistance * scoreProps.getDistanceNormalizationFactor(), 1.0); // 0-1 range
    return scoreProps.getNonIntersectionBaseScore()
        + (normalizedDistance * scoreProps.getNonIntersectionDistanceBonus()); // 50-100 range
  }

  /** Builds list of waypoint coordinates from nodes. */
  private List<double[]> buildWaypoints(List<RoadNode> nodes) {
    List<double[]> waypoints = new ArrayList<>();

    for (RoadNode node : nodes) {
      waypoints.add(new double[] {node.getLocation().getX(), node.getLocation().getY()});
    }

    return waypoints;
  }

  /** Generates human-readable notes about the route. */
  private String generateRouteNotes(int avoidedZones, double safetyScore, double distance) {
    StringBuilder notes = new StringBuilder();
    EvacuationProperties.SafetyScore scoreProps = properties.getSafetyScore();

    if (safetyScore >= scoreProps.getHighSafetyThreshold()) {
      notes.append("This is a safe evacuation route. ");
    } else if (safetyScore >= scoreProps.getMediumSafetyThreshold()) {
      notes.append("This route has moderate safety. Exercise caution. ");
    } else {
      notes.append("WARNING: This route passes near hazard zones. Use extreme caution. ");
    }

    if (avoidedZones > 0) {
      notes.append(String.format("Successfully avoiding %d hazard zone(s). ", avoidedZones));
    }

    if (distance > properties.getNotes().getLongRouteThresholdMeters()) {
      notes.append("This is a long route. Consider alternative transportation if available.");
    }

    return notes.toString().trim();
  }

  /** Checks if a point is within a hazard zone and logs a warning. */
  private void checkPointSafety(Point point, List<HazardZone> hazardZones, String pointType) {
    for (HazardZone zone : hazardZones) {
      if (zone.getGeometry().contains(point)) {
        log.warn(
            "{} point is within hazard zone: {} (severity: {})",
            pointType,
            zone.getId(),
            zone.getSeverity());
      }
    }
  }
}
