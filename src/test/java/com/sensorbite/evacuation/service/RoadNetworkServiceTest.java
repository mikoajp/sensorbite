/* Copyright (c) 2024 Sensorbite. All rights reserved. */
package com.sensorbite.evacuation.service;

import static org.junit.jupiter.api.Assertions.*;

import com.sensorbite.evacuation.domain.RoadNode;
import com.sensorbite.evacuation.exception.RoadNetworkLoadException;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;

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
                      "properties": {
                        "highway": "primary"
                      }
                    },
                    {
                      "type": "Feature",
                      "geometry": {
                        "type": "LineString",
                        "coordinates": [[21.01, 52.0], [21.01, 52.01]]
                      },
                      "properties": {
                        "highway": "secondary"
                      }
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
    assertTrue(graph.vertexSet().size() >= 2);
    assertTrue(graph.edgeSet().size() >= 1);
  }

  @Test
  void loadRoadNetwork_WithEmptyFeatures_ShouldThrowException() {
    // Arrange
    String geoJson =
        """
                {
                  "type": "FeatureCollection",
                  "features": []
                }
                """;

    InputStream inputStream = new ByteArrayInputStream(geoJson.getBytes());

    // Act & Assert
    assertThrows(
        RoadNetworkLoadException.class,
        () -> {
          roadNetworkService.loadRoadNetwork(inputStream);
        });
  }

  @Test
  void loadRoadNetwork_WithInvalidJSON_ShouldThrowException() {
    // Arrange
    String invalidJson = "{ invalid json }";
    InputStream inputStream = new ByteArrayInputStream(invalidJson.getBytes());

    // Act & Assert
    assertThrows(
        Exception.class,
        () -> {
          roadNetworkService.loadRoadNetwork(inputStream);
        });
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
                      "type": "Feature",
                      "geometry": {
                        "type": "LineString",
                        "coordinates": [[21.0, 52.0], [21.01, 52.0]]
                      },
                      "properties": {}
                    },
                    {
                      "type": "Feature",
                      "geometry": {
                        "type": "LineString",
                        "coordinates": [[21.01, 52.0], [21.02, 52.01]]
                      },
                      "properties": {}
                    }
                  ]
                }
                """;

    InputStream inputStream = new ByteArrayInputStream(geoJson.getBytes());
    Graph<RoadNode, DefaultWeightedEdge> graph = roadNetworkService.loadRoadNetwork(inputStream);

    Point targetPoint = geometryFactory.createPoint(new Coordinate(21.005, 52.0));

    // Act
    RoadNode nearestNode = roadNetworkService.findNearestNode(targetPoint, graph);

    // Assert
    assertNotNull(nearestNode);
    assertTrue(nearestNode.getLocation().distance(targetPoint) < 0.1);
  }

  @Test
  void findNearestNode_WithEmptyGraph_ShouldReturnNull() {
    // Arrange
    Graph<RoadNode, DefaultWeightedEdge> emptyGraph =
        new org.jgrapht.graph.SimpleWeightedGraph<>(DefaultWeightedEdge.class);
    Point targetPoint = geometryFactory.createPoint(new Coordinate(21.0, 52.0));

    // Act
    RoadNode nearestNode = roadNetworkService.findNearestNode(targetPoint, emptyGraph);

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
                      "properties": {
                        "highway": "primary"
                      }
                    }
                  ]
                }
                """;

    InputStream inputStream = new ByteArrayInputStream(geoJson.getBytes());

    // Act
    Graph<RoadNode, DefaultWeightedEdge> graph = roadNetworkService.loadRoadNetwork(inputStream);

    // Assert
    assertNotNull(graph);
    assertTrue(graph.vertexSet().size() >= 3);
    assertTrue(graph.edgeSet().size() >= 2);
  }
}
