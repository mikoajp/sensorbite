/* Copyright (c) 2024 Sensorbite. All rights reserved. */
package com.sensorbite.evacuation.service;

import static org.junit.jupiter.api.Assertions.*;

import com.sensorbite.evacuation.domain.RoadNode;
import com.sensorbite.evacuation.exception.RoadNetworkLoadException;
import com.sensorbite.evacuation.service.RoadNetworkService.RoadNetworkCacheEntry;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.index.strtree.STRtree;

class RoadNetworkServiceTest {

  private RoadNetworkService roadNetworkService;
  private GeometryFactory geometryFactory;

  @BeforeEach
  void setUp() {
    roadNetworkService = new RoadNetworkService();
    geometryFactory = new GeometryFactory();
  }

  @Test
  void loadRoadNetwork_WithValidGeoJSON_ShouldCreateGraph() {
    // Arrange
    String geoJson =
        """
                {
                  "type": "FeatureCollection",
                  "features": [
                    {
                      "type": "Feature",
                      "geometry": {
                        "type": "LineString",
                        "coordinates": [[21.0, 52.0], [21.01, 52.0]]
                      },
                      "properties": { "highway": "primary" }
                    }
                  ]
                }
                """;

    InputStream inputStream = new ByteArrayInputStream(geoJson.getBytes());

    // Act
    Graph<RoadNode, DefaultWeightedEdge> graph = roadNetworkService.loadRoadNetwork(inputStream);

    // Assert
    assertNotNull(graph);
    assertFalse(graph.vertexSet().isEmpty());
    assertFalse(graph.edgeSet().isEmpty());
  }

  @Test
  void loadRoadNetwork_WithEmptyFeatures_ShouldThrowException() {
    // Arrange
    String geoJson = """
        { "type": "FeatureCollection", "features": [] }
        """;
    InputStream inputStream = new ByteArrayInputStream(geoJson.getBytes());

    // Act & Assert
    assertThrows(
        RoadNetworkLoadException.class, () -> roadNetworkService.loadRoadNetwork(inputStream));
  }

  @Test
  void loadRoadNetwork_WithInvalidJSON_ShouldThrowException() {
    // Arrange
    String invalidJson = "{ invalid json }";
    InputStream inputStream = new ByteArrayInputStream(invalidJson.getBytes());

    // Act & Assert
    assertThrows(Exception.class, () -> roadNetworkService.loadRoadNetwork(inputStream));
  }

  @Test
  void findNearestNode_ShouldReturnClosestNode() {
    // Arrange
    String geoJson =
        """
                {
                  "type": "FeatureCollection",
                  "features": [
                    {
                      "type": "Feature", "geometry": { "type": "LineString", "coordinates": [[10, 10], [20, 10]] }
                    },
                    {
                      "type": "Feature", "geometry": { "type": "LineString", "coordinates": [[20, 10], [20, 20]] }
                    }
                  ]
                }
                """;

    InputStream inputStream = new ByteArrayInputStream(geoJson.getBytes());
    Graph<RoadNode, DefaultWeightedEdge> graph = roadNetworkService.loadRoadNetwork(inputStream);
    RoadNetworkCacheEntry cacheEntry = buildTestCacheEntry(graph);

    Point targetPoint = geometryFactory.createPoint(new Coordinate(19.9, 10));

    // Act
    RoadNode nearestNode = roadNetworkService.findNearestNode(targetPoint, cacheEntry);

    // Assert
    assertNotNull(nearestNode);
    // The closest node should be (20, 10)
    assertEquals(20.0, nearestNode.getLocation().getX());
    assertEquals(10.0, nearestNode.getLocation().getY());
  }

  @Test
  void findNearestNode_WithEmptyGraph_ShouldReturnNull() {
    // Arrange
    Graph<RoadNode, DefaultWeightedEdge> emptyGraph =
        new org.jgrapht.graph.SimpleWeightedGraph<>(DefaultWeightedEdge.class);
    RoadNetworkCacheEntry emptyCacheEntry = buildTestCacheEntry(emptyGraph);
    Point targetPoint = geometryFactory.createPoint(new Coordinate(21.0, 52.0));

    // Act
    RoadNode nearestNode = roadNetworkService.findNearestNode(targetPoint, emptyCacheEntry);

    // Assert
    assertNull(nearestNode);
  }

  @Test
  void loadRoadNetwork_WithMultiLineString_ShouldProcessAllSegments() {
    // Arrange
    String geoJson =
        """
                {
                  "type": "FeatureCollection",
                  "features": [
                    {
                      "type": "Feature",
                      "geometry": {
                        "type": "MultiLineString",
                        "coordinates": [
                          [[21.0, 52.0], [21.01, 52.0]],
                          [[21.01, 52.0], [21.02, 52.0]]
                        ]
                      },
                      "properties": { "highway": "primary" }
                    }
                  ]
                }
                """;

    InputStream inputStream = new ByteArrayInputStream(geoJson.getBytes());

    // Act
    Graph<RoadNode, DefaultWeightedEdge> graph = roadNetworkService.loadRoadNetwork(inputStream);

    // Assert
    assertNotNull(graph);
    assertEquals(3, graph.vertexSet().size());
    assertEquals(2, graph.edgeSet().size());
  }

  private RoadNetworkCacheEntry buildTestCacheEntry(Graph<RoadNode, DefaultWeightedEdge> graph) {
    STRtree spatialIndex = new STRtree();
    for (RoadNode node : graph.vertexSet()) {
      spatialIndex.insert(node.getLocation().getEnvelopeInternal(), node);
    }
    spatialIndex.build();
    return new RoadNetworkCacheEntry(graph, spatialIndex);
  }
}
