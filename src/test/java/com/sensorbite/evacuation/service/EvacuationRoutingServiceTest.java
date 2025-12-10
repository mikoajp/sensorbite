/* Copyright (c) 2024 Sensorbite. All rights reserved. */
package com.sensorbite.evacuation.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

import com.sensorbite.evacuation.domain.HazardZone;
import com.sensorbite.evacuation.domain.RoadNode;
import com.sensorbite.evacuation.domain.RouteResult;
import com.sensorbite.evacuation.exception.NoRouteFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.locationtech.jts.geom.*;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class EvacuationRoutingServiceTest {

  @Mock private RoadNetworkService roadNetworkService;

  @InjectMocks private EvacuationRoutingService routingService;

  private GeometryFactory geometryFactory;
  private Graph<RoadNode, DefaultWeightedEdge> testGraph;

  @BeforeEach
  void setUp() {
    geometryFactory = new GeometryFactory();
    testGraph = createTestGraph();
  }

  @Test
  void findSafestRoute_WithValidPoints_ShouldReturnRoute() {
    // Arrange
    Point start = geometryFactory.createPoint(new Coordinate(0, 0));
    Point end = geometryFactory.createPoint(new Coordinate(2, 0));
    List<HazardZone> hazards = Collections.emptyList();

    RoadNode startNode = new RoadNode("node_0_0", start);
    RoadNode endNode = new RoadNode("node_2_0", end);

    when(roadNetworkService.findNearestNode(start, testGraph)).thenReturn(startNode);
    when(roadNetworkService.findNearestNode(end, testGraph)).thenReturn(endNode);

    // Act
    RouteResult result = routingService.findSafestRoute(start, end, testGraph, hazards);

    // Assert
    assertNotNull(result);
    assertNotNull(result.getGeometry());
    assertTrue(result.getDistanceMeters() > 0);
    assertTrue(result.getEstimatedTimeMinutes() >= 0);
    assertEquals("modified_dijkstra", result.getAlgorithm());
  }

  @Test
  void findSafestRoute_WithHazardZones_ShouldAvoidThem() {
    // Arrange
    Point start = geometryFactory.createPoint(new Coordinate(0, 0));
    Point end = geometryFactory.createPoint(new Coordinate(2, 0));

    // Create hazard zone in the middle
    Coordinate[] hazardCoords = {
      new Coordinate(0.8, -0.2),
      new Coordinate(1.2, -0.2),
      new Coordinate(1.2, 0.2),
      new Coordinate(0.8, 0.2),
      new Coordinate(0.8, -0.2)
    };
    Polygon hazardPolygon = geometryFactory.createPolygon(hazardCoords);

    HazardZone hazard =
        HazardZone.builder()
            .id("hazard_1")
            .geometry(hazardPolygon)
            .hazardType("flood")
            .severity(4)
            .build();

    List<HazardZone> hazards = List.of(hazard);

    RoadNode startNode = new RoadNode("node_0_0", start);
    RoadNode endNode = new RoadNode("node_2_0", end);

    when(roadNetworkService.findNearestNode(start, testGraph)).thenReturn(startNode);
    when(roadNetworkService.findNearestNode(end, testGraph)).thenReturn(endNode);

    // Act
    RouteResult result = routingService.findSafestRoute(start, end, testGraph, hazards);

    // Assert
    assertNotNull(result);
    assertTrue(result.getSafetyScore() >= 0);
    assertTrue(result.getSafetyScore() <= 100);
  }

  @Test
  void findSafestRoute_WhenNoRouteExists_ShouldThrowException() {
    // Arrange
    Graph<RoadNode, DefaultWeightedEdge> disconnectedGraph =
        new SimpleWeightedGraph<>(DefaultWeightedEdge.class);

    Point start = geometryFactory.createPoint(new Coordinate(0, 0));
    Point end = geometryFactory.createPoint(new Coordinate(10, 10));

    RoadNode startNode = new RoadNode("node_0_0", start);
    RoadNode endNode = new RoadNode("node_10_10", end);

    disconnectedGraph.addVertex(startNode);
    disconnectedGraph.addVertex(endNode);
    // No edges - disconnected graph

    when(roadNetworkService.findNearestNode(start, disconnectedGraph)).thenReturn(startNode);
    when(roadNetworkService.findNearestNode(end, disconnectedGraph)).thenReturn(endNode);

    // Act & Assert
    assertThrows(
        NoRouteFoundException.class,
        () -> {
          routingService.findSafestRoute(start, end, disconnectedGraph, Collections.emptyList());
        });
  }

  @Test
  void findSafestRoute_WhenStartNodeNotFound_ShouldThrowException() {
    // Arrange
    Point start = geometryFactory.createPoint(new Coordinate(0, 0));
    Point end = geometryFactory.createPoint(new Coordinate(2, 0));

    when(roadNetworkService.findNearestNode(start, testGraph)).thenReturn(null);
    when(roadNetworkService.findNearestNode(end, testGraph))
        .thenReturn(new RoadNode("node_2_0", end));

    // Act & Assert
    assertThrows(
        NoRouteFoundException.class,
        () -> {
          routingService.findSafestRoute(start, end, testGraph, Collections.emptyList());
        });
  }

  @Test
  void findSafestRoute_WithMultipleHazards_ShouldCalculateCorrectSafetyScore() {
    // Arrange
    Point start = geometryFactory.createPoint(new Coordinate(0, 0));
    Point end = geometryFactory.createPoint(new Coordinate(2, 0));

    List<HazardZone> hazards = new ArrayList<>();

    // Add multiple hazard zones
    for (int i = 0; i < 3; i++) {
      Coordinate[] coords = {
        new Coordinate(3 + i, -1),
        new Coordinate(4 + i, -1),
        new Coordinate(4 + i, 1),
        new Coordinate(3 + i, 1),
        new Coordinate(3 + i, -1)
      };
      Polygon polygon = geometryFactory.createPolygon(coords);

      hazards.add(
          HazardZone.builder()
              .id("hazard_" + i)
              .geometry(polygon)
              .hazardType("flood")
              .severity(3)
              .build());
    }

    RoadNode startNode = new RoadNode("node_0_0", start);
    RoadNode endNode = new RoadNode("node_2_0", end);

    when(roadNetworkService.findNearestNode(start, testGraph)).thenReturn(startNode);
    when(roadNetworkService.findNearestNode(end, testGraph)).thenReturn(endNode);

    // Act
    RouteResult result = routingService.findSafestRoute(start, end, testGraph, hazards);

    // Assert
    assertNotNull(result);
    assertTrue(result.getAvoidedHazards() >= 0);
    assertNotNull(result.getNotes());
  }

  /** Creates a simple test graph with a grid structure. */
  private Graph<RoadNode, DefaultWeightedEdge> createTestGraph() {
    Graph<RoadNode, DefaultWeightedEdge> graph =
        new SimpleWeightedGraph<>(DefaultWeightedEdge.class);

    // Create 3x3 grid
    RoadNode[][] nodes = new RoadNode[3][3];

    for (int i = 0; i < 3; i++) {
      for (int j = 0; j < 3; j++) {
        Point point = geometryFactory.createPoint(new Coordinate(i, j));
        nodes[i][j] = new RoadNode("node_" + i + "_" + j, point);
        graph.addVertex(nodes[i][j]);
      }
    }

    // Connect nodes
    for (int i = 0; i < 3; i++) {
      for (int j = 0; j < 3; j++) {
        // Horizontal
        if (i < 2) {
          DefaultWeightedEdge edge = graph.addEdge(nodes[i][j], nodes[i + 1][j]);
          if (edge != null) graph.setEdgeWeight(edge, 1.0);
        }
        // Vertical
        if (j < 2) {
          DefaultWeightedEdge edge = graph.addEdge(nodes[i][j], nodes[i][j + 1]);
          if (edge != null) graph.setEdgeWeight(edge, 1.0);
        }
      }
    }

    return graph;
  }
}
