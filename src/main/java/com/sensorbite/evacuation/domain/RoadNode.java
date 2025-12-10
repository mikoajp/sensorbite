/* Copyright (c) 2024 Sensorbite. All rights reserved. */
package com.sensorbite.evacuation.domain;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import org.locationtech.jts.geom.Point;

/**
 * Represents a node (intersection) in the road network. Each node has a unique ID, geographic
 * location, and connections to road segments.
 */
public class RoadNode {

  private final String id;
  private final Point location;
  private final Set<String> connectedSegments;

  public RoadNode(String id, Point location) {
    this.id = Objects.requireNonNull(id, "Node ID cannot be null");
    this.location = Objects.requireNonNull(location, "Location cannot be null");
    this.connectedSegments = new HashSet<>();
  }

  public String getId() {
    return id;
  }

  public Point getLocation() {
    return location;
  }

  public Set<String> getConnectedSegments() {
    return new HashSet<>(connectedSegments);
  }

  public void addConnectedSegment(String segmentId) {
    this.connectedSegments.add(segmentId);
  }

  public double getX() {
    return location.getX();
  }

  public double getY() {
    return location.getY();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    RoadNode roadNode = (RoadNode) o;
    return Objects.equals(id, roadNode.id);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id);
  }

  @Override
  public String toString() {
    return "RoadNode{"
        + "id='"
        + id
        + '\''
        + ", location="
        + location
        + ", connectedSegments="
        + connectedSegments.size()
        + '}';
  }
}
