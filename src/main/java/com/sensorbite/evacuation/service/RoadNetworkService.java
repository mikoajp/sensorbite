/* Copyright (c) 2024 Sensorbite. All rights reserved. */
package com.sensorbite.evacuation.service;

import com.sensorbite.evacuation.domain.RoadNode;
import com.sensorbite.evacuation.exception.RoadNetworkLoadException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.geotools.geojson.feature.FeatureJSON;
import org.geotools.geojson.geom.GeometryJSON;
import org.geotools.referencing.GeodeticCalculator;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;
import org.locationtech.jts.geom.*;
import org.locationtech.jts.index.strtree.STRtree;
import org.opengis.feature.simple.SimpleFeature;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

/**
 * Service responsible for loading and managing road network data from GeoJSON files. Converts
 * geographic data into a graph structure suitable for routing algorithms. This service is stateless,
 * with caching at the service method level.
 */
@Service
@Slf4j
public class RoadNetworkService {

  private static final String SAMPLE_NETWORK_PATH = "data/sample_network.geojson";

  // Use a single GeodeticCalculator for WGS84, which is thread-safe for this usage.
  private static final GeodeticCalculator GEO_CALCULATOR =
      new GeodeticCalculator(DefaultGeographicCRS.WGS84);

  /**
   * A cache entry holding the road network graph and a spatial index for fast node lookups.
   *
   * @param graph The road network graph.
   * @param spatialIndex The STR-tree spatial index for the graph nodes.
   */
  @Value
  public static class RoadNetworkCacheEntry {
    Graph<RoadNode, DefaultWeightedEdge> graph;
    STRtree spatialIndex;
  }

  /**
   * Gets a road network graph by its name. This is a convenience method that delegates to {@link
   * #getRoadNetworkCacheEntry(String)}.
   *
   * @param networkName The name of the network file.
   * @return A graph representation of the road network.
   */
  public Graph<RoadNode, DefaultWeightedEdge> getRoadNetworkGraph(String networkName) {
    return getRoadNetworkCacheEntry(networkName).getGraph();
  }

  /**
   * Gets a cached entry containing the road network graph and its spatial index. If not in the
   * cache, it loads the network, builds the graph and index, and caches the result.
   *
   * @param networkName The name of the network file, or null/blank for the default sample network.
   * @return A {@link RoadNetworkCacheEntry} containing the graph and spatial index.
   */
  @Cacheable("roadNetworks")
  public RoadNetworkCacheEntry getRoadNetworkCacheEntry(String networkName) {
    String resourcePath =
        (networkName != null && !networkName.isBlank())
            ? "data/networks/" + networkName
            : SAMPLE_NETWORK_PATH;

    ClassPathResource resource = new ClassPathResource(resourcePath);

    Graph<RoadNode, DefaultWeightedEdge> graph;
    if (resource.exists()) {
      try (InputStream inputStream = resource.getInputStream()) {
        log.info("Loading road network from resource: {}", resourcePath);
        graph = loadRoadNetwork(inputStream);
      } catch (IOException | RoadNetworkLoadException e) {
        log.warn(
            "Failed to load network '{}', falling back to sample network. Error: {}",
            resourcePath,
            e.getMessage());
        graph = createSampleNetwork();
      }
    } else {
      log.warn("Network resource not found: {}, falling back to sample network.", resourcePath);
      graph = createSampleNetwork();
    }

    // Build spatial index from the loaded graph
    STRtree spatialIndex = buildSpatialIndex(graph);
    return new RoadNetworkCacheEntry(graph, spatialIndex);
  }

  /**
   * Loads a road network from an InputStream. This method is stateless and performs a full load of
   * the network data each time it's called.
   */
  Graph<RoadNode, DefaultWeightedEdge> loadRoadNetwork(InputStream inputStream)
      throws RoadNetworkLoadException {

    long startTime = System.currentTimeMillis();
    Graph<RoadNode, DefaultWeightedEdge> graph = new SimpleWeightedGraph<>(DefaultWeightedEdge.class);
    Map<String, RoadNode> nodeCache = new HashMap<>();

    try {
      FeatureJSON featureJSON = new FeatureJSON(new GeometryJSON(15));
      var featureIterator = featureJSON.streamFeatureCollection(inputStream);

      int featureCount = 0;
      while (featureIterator.hasNext()) {
        SimpleFeature feature = featureIterator.next();
        processFeature(feature, graph, nodeCache);
        featureCount++;
      }

      if (graph.vertexSet().isEmpty()) {
        throw new RoadNetworkLoadException("No valid road segments found in GeoJSON stream");
      }

      long loadTime = System.currentTimeMillis() - startTime;
      log.info(
          "Road network from stream loaded successfully: {} features, {} nodes, {} edges in {}ms",
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

  private void processFeature(
      SimpleFeature feature,
      Graph<RoadNode, DefaultWeightedEdge> graph,
      Map<String, RoadNode> nodeCache) {
    try {
      Geometry geometry = (Geometry) feature.getDefaultGeometry();
      if (geometry == null) {
        log.warn("Feature {} has no geometry, skipping", feature.getID());
        return;
      }

      if (geometry instanceof LineString) {
        processLineString((LineString) geometry, graph, nodeCache);
      } else if (geometry instanceof MultiLineString) {
        MultiLineString multiLineString = (MultiLineString) geometry;
        for (int i = 0; i < multiLineString.getNumGeometries(); i++) {
          processLineString((LineString) multiLineString.getGeometryN(i), graph, nodeCache);
        }
      }
    } catch (Exception e) {
      log.warn("Error processing feature {}: {}", feature.getID(), e.getMessage());
    }
  }

  private void processLineString(
      LineString lineString,
      Graph<RoadNode, DefaultWeightedEdge> graph,
      Map<String, RoadNode> nodeCache) {
    if (lineString.getNumPoints() < 2) {
      return;
    }

    Coordinate startCoord = lineString.getCoordinateN(0);
    Coordinate endCoord = lineString.getCoordinateN(lineString.getNumPoints() - 1);

    RoadNode startNode = getOrCreateNode(startCoord, nodeCache);
    RoadNode endNode = getOrCreateNode(endCoord, nodeCache);

    graph.addVertex(startNode);
    graph.addVertex(endNode);

    DefaultWeightedEdge edge = graph.addEdge(startNode, endNode);
    if (edge != null) {
      double length = calculateAccurateDistance(lineString);
      graph.setEdgeWeight(edge, length);
    }
  }

  private double calculateAccurateDistance(LineString lineString) {
    double totalDistance = 0;
    Coordinate[] coords = lineString.getCoordinates();
    for (int i = 0; i < coords.length - 1; i++) {
      synchronized (GEO_CALCULATOR) {
        GEO_CALCULATOR.setStartingGeographicPoint(coords[i].x, coords[i].y);
        GEO_CALCULATOR.setDestinationGeographicPoint(coords[i + 1].x, coords[i + 1].y);
        totalDistance += GEO_CALCULATOR.getOrthodromicDistance();
      }
    }
    return totalDistance;
  }

  private RoadNode getOrCreateNode(Coordinate coord, Map<String, RoadNode> nodeCache) {
    String nodeId = String.format("node_%.6f_%.6f", coord.x, coord.y);
    return nodeCache.computeIfAbsent(
        nodeId,
        id -> {
          GeometryFactory factory = new GeometryFactory();
          Point point = factory.createPoint(coord);
          return new RoadNode(id, point);
        });
  }

  /**
   * Finds the nearest node in the network to the given point using a spatial index.
   *
   * @param point The target point.
   * @param network The cached network entry containing the spatial index.
   * @return The nearest {@link RoadNode}, or null if the network is empty.
   */
  public RoadNode findNearestNode(Point point, RoadNetworkCacheEntry network) {
    if (network == null || network.getSpatialIndex().isEmpty()) {
      return null;
    }
    STRtree index = network.getSpatialIndex();

    // The ItemDistance lambda calculates the distance between an item in the index (item1)
    // and the query item (item2). In our case, item1 wraps a RoadNode and item2 wraps the
    // query Point.
    return (RoadNode)
        index.nearestNeighbour(
            point.getEnvelopeInternal(),
            point,
            (item1, item2) -> {
              Point p1 = ((RoadNode) item1.getItem()).getLocation();
              Point p2 = (Point) item2.getItem();
              return p1.distance(p2);
            });
  }

  /** Builds an STR-tree spatial index from the nodes of a graph. */
  private STRtree buildSpatialIndex(Graph<RoadNode, DefaultWeightedEdge> graph) {
    log.info("Building spatial index for {} nodes...", graph.vertexSet().size());
    long startTime = System.currentTimeMillis();
    STRtree index = new STRtree();
    for (RoadNode node : graph.vertexSet()) {
      index.insert(node.getLocation().getEnvelopeInternal(), node);
    }
    index.build();
    long buildTime = System.currentTimeMillis() - startTime;
    log.info("Spatial index built in {}ms.", buildTime);
    return index;
  }

  private Graph<RoadNode, DefaultWeightedEdge> createSampleNetwork() {
    log.info("Creating sample road network");
    Graph<RoadNode, DefaultWeightedEdge> graph = new SimpleWeightedGraph<>(DefaultWeightedEdge.class);
    GeometryFactory factory = new GeometryFactory();
    RoadNode[][] nodes = new RoadNode[5][5];

    for (int i = 0; i < 5; i++) {
      for (int j = 0; j < 5; j++) {
        Point point = factory.createPoint(new Coordinate(21.0 + (j * 0.01), 52.0 + (i * 0.01)));
        nodes[i][j] = new RoadNode("node_" + i + "_" + j, point);
        graph.addVertex(nodes[i][j]);
      }
    }

    for (int i = 0; i < 5; i++) {
      for (int j = 0; j < 5; j++) {
        if (j < 4) {
          synchronized (GEO_CALCULATOR) {
            GEO_CALCULATOR.setStartingGeographicPoint(
                nodes[i][j].getLocation().getX(), nodes[i][j].getLocation().getY());
            GEO_CALCULATOR.setDestinationGeographicPoint(
                nodes[i][j + 1].getLocation().getX(), nodes[i][j + 1].getLocation().getY());
            graph.setEdgeWeight(
                graph.addEdge(nodes[i][j], nodes[i][j + 1]),
                GEO_CALCULATOR.getOrthodromicDistance());
          }
        }
        if (i < 4) {
          synchronized (GEO_CALCULATOR) {
            GEO_CALCULATOR.setStartingGeographicPoint(
                nodes[i][j].getLocation().getX(), nodes[i][j].getLocation().getY());
            GEO_CALCULATOR.setDestinationGeographicPoint(
                nodes[i + 1][j].getLocation().getX(), nodes[i + 1][j].getLocation().getY());
            graph.setEdgeWeight(
                graph.addEdge(nodes[i][j], nodes[i + 1][j]),
                GEO_CALCULATOR.getOrthodromicDistance());
          }
        }
      }
    }
    log.info(
        "Sample network created: {} nodes, {} edges",
        graph.vertexSet().size(),
        graph.edgeSet().size());
    return graph;
  }
}
