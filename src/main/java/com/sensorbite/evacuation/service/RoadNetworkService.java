/* Copyright (c) 2024 Sensorbite. All rights reserved. */
package com.sensorbite.evacuation.service;

import com.sensorbite.evacuation.domain.RoadNode;
import com.sensorbite.evacuation.domain.RoadSegment;
import com.sensorbite.evacuation.exception.RoadNetworkLoadException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import lombok.extern.slf4j.Slf4j;
import org.geotools.geojson.feature.FeatureJSON;
import org.geotools.geojson.geom.GeometryJSON;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;
import org.locationtech.jts.geom.*;
import org.opengis.feature.simple.SimpleFeature;
import org.springframework.stereotype.Service;

/**
 * Service responsible for loading and managing road network data from GeoJSON files. Converts
 * geographic data into a graph structure suitable for routing algorithms.
 */
@Service
@Slf4j
public class RoadNetworkService {

  private static final double COORDINATE_PRECISION = 6; // decimal places
  private static final double NODE_MERGE_THRESHOLD = 0.00001; // ~1 meter

  private final Map<String, RoadNode> nodeCache = new HashMap<>();
  private final Map<String, RoadSegment> segmentCache = new HashMap<>();

  /**
   * Loads a road network from a GeoJSON file and converts it to a graph structure.
   *
   * @param geojsonFile The GeoJSON file containing road network data
   * @return A graph representation of the road network
   * @throws RoadNetworkLoadException if the file cannot be loaded or parsed
   */
  public Graph<RoadNode, DefaultWeightedEdge> loadRoadNetwork(File geojsonFile)
      throws RoadNetworkLoadException {

    log.info("Loading road network from file: {}", geojsonFile.getAbsolutePath());
    long startTime = System.currentTimeMillis();

    try {
      // Clear caches
      nodeCache.clear();
      segmentCache.clear();

      // Create graph
      Graph<RoadNode, DefaultWeightedEdge> graph =
          new SimpleWeightedGraph<>(DefaultWeightedEdge.class);

      // Parse GeoJSON
      FeatureJSON featureJSON = new FeatureJSON(new GeometryJSON(15));

      try (InputStream inputStream = new FileInputStream(geojsonFile)) {
        // Read features from GeoJSON
        var featureIterator = featureJSON.streamFeatureCollection(inputStream);

        int featureCount = 0;
        while (featureIterator.hasNext()) {
          SimpleFeature feature = featureIterator.next();
          processFeature(feature, graph);
          featureCount++;
        }

        if (graph.vertexSet().isEmpty()) {
          throw new RoadNetworkLoadException("No valid road segments found in GeoJSON file");
        }

        long loadTime = System.currentTimeMillis() - startTime;
        log.info(
            "Road network loaded successfully: {} features, {} nodes, {} edges in {}ms",
            featureCount,
            graph.vertexSet().size(),
            graph.edgeSet().size(),
            loadTime);

        return graph;
      }

    } catch (IOException e) {
      log.error("Failed to load road network from file: {}", geojsonFile.getAbsolutePath(), e);
      throw new RoadNetworkLoadException("Failed to load road network: " + e.getMessage(), e);
    }
  }

  /** Loads a road network from an InputStream (useful for testing and resources). */
  public Graph<RoadNode, DefaultWeightedEdge> loadRoadNetwork(InputStream inputStream)
      throws RoadNetworkLoadException {

    log.info("Loading road network from input stream");
    long startTime = System.currentTimeMillis();

    try {
      // Clear caches
      nodeCache.clear();
      segmentCache.clear();

      // Create graph
      Graph<RoadNode, DefaultWeightedEdge> graph =
          new SimpleWeightedGraph<>(DefaultWeightedEdge.class);

      // Parse GeoJSON
      FeatureJSON featureJSON = new FeatureJSON(new GeometryJSON(15));
      var featureIterator = featureJSON.streamFeatureCollection(inputStream);

      int featureCount = 0;
      while (featureIterator.hasNext()) {
        SimpleFeature feature = featureIterator.next();
        processFeature(feature, graph);
        featureCount++;
      }

      if (graph.vertexSet().isEmpty()) {
        throw new RoadNetworkLoadException("No valid road segments found in GeoJSON file");
      }

      long loadTime = System.currentTimeMillis() - startTime;
      log.info(
          "Road network loaded successfully: {} features, {} nodes, {} edges in {}ms",
          featureCount,
          graph.vertexSet().size(),
          graph.edgeSet().size(),
          loadTime);

      return graph;

    } catch (IOException e) {
      log.error("Failed to load road network from input stream", e);
      throw new RoadNetworkLoadException("Failed to load road network: " + e.getMessage(), e);
    }
  }

  /** Processes a single GeoJSON feature and adds it to the graph. */
  private void processFeature(SimpleFeature feature, Graph<RoadNode, DefaultWeightedEdge> graph) {
    try {
      Geometry geometry = (Geometry) feature.getDefaultGeometry();

      if (geometry == null) {
        log.warn("Feature {} has no geometry, skipping", feature.getID());
        return;
      }

      // Handle different geometry types
      if (geometry instanceof LineString) {
        processLineString((LineString) geometry, feature, graph);
      } else if (geometry instanceof MultiLineString) {
        MultiLineString multiLineString = (MultiLineString) geometry;
        for (int i = 0; i < multiLineString.getNumGeometries(); i++) {
          processLineString((LineString) multiLineString.getGeometryN(i), feature, graph);
        }
      } else {
        log.debug("Unsupported geometry type: {}, skipping", geometry.getGeometryType());
      }

    } catch (Exception e) {
      log.warn("Error processing feature {}: {}", feature.getID(), e.getMessage());
    }
  }

  /** Processes a LineString geometry and adds nodes and edges to the graph. */
  private void processLineString(
      LineString lineString, SimpleFeature feature, Graph<RoadNode, DefaultWeightedEdge> graph) {

    if (lineString.getNumPoints() < 2) {
      log.warn("LineString has less than 2 points, skipping");
      return;
    }

    // Extract start and end points
    Coordinate startCoord = lineString.getCoordinateN(0);
    Coordinate endCoord = lineString.getCoordinateN(lineString.getNumPoints() - 1);

    // Create or retrieve nodes
    RoadNode startNode = getOrCreateNode(startCoord);
    RoadNode endNode = getOrCreateNode(endCoord);

    // Add nodes to graph
    graph.addVertex(startNode);
    graph.addVertex(endNode);

    // Create road segment
    String segmentId = generateSegmentId(startNode, endNode);
    double length = lineString.getLength() * 111320; // Convert degrees to meters (approximate)
    String roadType = extractRoadType(feature);

    RoadSegment segment =
        new RoadSegment(segmentId, lineString, length, roadType, startNode, endNode);

    segmentCache.put(segmentId, segment);

    // Add edge to graph
    DefaultWeightedEdge edge = graph.addEdge(startNode, endNode);
    if (edge != null) {
      graph.setEdgeWeight(edge, segment.getWeight());
    }
  }

  /** Gets an existing node or creates a new one for the given coordinate. */
  private RoadNode getOrCreateNode(Coordinate coord) {
    String nodeId = generateNodeId(coord);

    return nodeCache.computeIfAbsent(
        nodeId,
        id -> {
          GeometryFactory factory = new GeometryFactory();
          Point point = factory.createPoint(coord);
          return new RoadNode(id, point);
        });
  }

  /** Generates a unique ID for a node based on its coordinates. */
  private String generateNodeId(Coordinate coord) {
    return String.format("node_%.6f_%.6f", coord.x, coord.y);
  }

  /** Generates a unique ID for a road segment. */
  private String generateSegmentId(RoadNode start, RoadNode end) {
    return "segment_" + start.getId() + "_" + end.getId();
  }

  /** Extracts road type from feature properties. */
  private String extractRoadType(SimpleFeature feature) {
    Object highway = feature.getAttribute("highway");
    if (highway != null) {
      return highway.toString();
    }

    Object type = feature.getAttribute("type");
    if (type != null) {
      return type.toString();
    }

    return "unknown";
  }

  /**
   * Finds the nearest node in the graph to the given point.
   *
   * @param point The target point
   * @param graph The road network graph
   * @return The nearest node, or null if graph is empty
   */
  public RoadNode findNearestNode(Point point, Graph<RoadNode, DefaultWeightedEdge> graph) {
    if (graph.vertexSet().isEmpty()) {
      return null;
    }

    RoadNode nearest = null;
    double minDistance = Double.MAX_VALUE;

    for (RoadNode node : graph.vertexSet()) {
      double distance = point.distance(node.getLocation());
      if (distance < minDistance) {
        minDistance = distance;
        nearest = node;
      }
    }

    log.debug(
        "Found nearest node {} at distance {} from point {}",
        nearest != null ? nearest.getId() : "null",
        minDistance,
        point);

    return nearest;
  }

  /** Gets a cached road segment by ID. */
  public RoadSegment getSegment(String segmentId) {
    return segmentCache.get(segmentId);
  }

  /** Gets all cached road segments. */
  public Collection<RoadSegment> getAllSegments() {
    return segmentCache.values();
  }
}
