/* Copyright (c) 2024 Sensorbite. All rights reserved. */
package com.sensorbite.evacuation.exception;

/** Exception thrown when no valid evacuation route can be found between two points. */
public class NoRouteFoundException extends RuntimeException {

  public NoRouteFoundException(String message) {
    super(message);
  }

  public NoRouteFoundException(String message, Throwable cause) {
    super(message, cause);
  }
}
