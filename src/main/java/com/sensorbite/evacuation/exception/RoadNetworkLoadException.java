/* Copyright (c) 2024 Sensorbite. All rights reserved. */
package com.sensorbite.evacuation.exception;

/** Exception thrown when there's an error loading or parsing the road network data. */
public class RoadNetworkLoadException extends RuntimeException {

  public RoadNetworkLoadException(String message) {
    super(message);
  }

  public RoadNetworkLoadException(String message, Throwable cause) {
    super(message, cause);
  }
}
