/* Copyright (c) 2024 Sensorbite. All rights reserved. */
package com.sensorbite.evacuation.domain;

import java.util.Objects;
import org.locationtech.jts.geom.LineString;

/**
 * Represents a road segment (edge) connecting two nodes in the road network. Contains geometric
 * information and properties like length and road type.
 */
public class RoadSegment {

  private final String id;
  private final LineString geometry;
  private final double lengthMeters;
  private final String roadType;
  private final RoadNode startNode;
  private final RoadNode endNode;
  private boolean isBlocked;

  public RoadSegment(
      String id,
      LineString geometry,
      double lengthMeters,
      String roadType,
      RoadNode startNode,
      RoadNode endNode) {
    this.id = Objects.requireNonNull(id, "Segment ID cannot be null");
    this.geometry = Objects.requireNonNull(geometry, "Geometry cannot be null");
    this.lengthMeters = lengthMeters;
    this.roadType = roadType != null ? roadType : "unknown";
    this.startNode = Objects.requireNonNull(startNode, "Start node cannot be null");
    this.endNode = Objects.requireNonNull(endNode, "End node cannot be null");
    this.isBlocked = false;
  }

  public String getId() {
    return id;
  }

  public LineString getGeometry() {
    return geometry;
  }

  public double getLengthMeters() {
    return lengthMeters;
  }

  public String getRoadType() {
    return roadType;
  }

  public RoadNode getStartNode() {
    return startNode;
  }

  public RoadNode getEndNode() {
    return endNode;
  }

  public boolean isBlocked() {
    return isBlocked;
  }

  public void setBlocked(boolean blocked) {
    isBlocked = blocked;
  }

  /**
   * Calculates the weight of this segment for routing purposes. Can be extended to include factors
   * like road type, traffic, etc.
   */
  public double getWeight() {
    if (isBlocked) {
      return Double.POSITIVE_INFINITY;
    }
    return lengthMeters;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    RoadSegment that = (RoadSegment) o;
    return Objects.equals(id, that.id);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id);
  }

  @Override
  public String toString() {
    return "RoadSegment{"
        + "id='"
        + id
        + '\''
        + ", lengthMeters="
        + lengthMeters
        + ", roadType='"
        + roadType
        + '\''
        + ", isBlocked="
        + isBlocked
        + '}';
  }
}
