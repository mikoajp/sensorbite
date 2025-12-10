/* Copyright (c) 2024 Sensorbite. All rights reserved. */
package com.sensorbite.exception;

public class ResourceAlreadyExistsException extends RuntimeException {
  public ResourceAlreadyExistsException(String message) {
    super(message);
  }
}
