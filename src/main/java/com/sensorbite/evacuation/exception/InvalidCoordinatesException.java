/* Copyright (c) 2024 Sensorbite. All rights reserved. */
package com.sensorbite.evacuation.exception;

/** Exception thrown when provided coordinates are invalid or out of bounds. */
public class InvalidCoordinatesException extends RuntimeException {

  public InvalidCoordinatesException(String message) {
    super(message);
  }

  public InvalidCoordinatesException(String message, Throwable cause) {
    super(message, cause);
  }
}
