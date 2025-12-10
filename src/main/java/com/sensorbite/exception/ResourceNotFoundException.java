/* Copyright (c) 2024 Sensorbite. All rights reserved. */
package com.sensorbite.exception;

public class ResourceNotFoundException extends RuntimeException {
  public ResourceNotFoundException(String message) {
    super(message);
  }
}
