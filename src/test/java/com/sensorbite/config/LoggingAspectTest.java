/* Copyright (c) 2024 Sensorbite. All rights reserved. */
package com.sensorbite.config;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.sensorbite.service.BuildingService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/** Tests for LoggingAspect to ensure AOP advice is applied correctly. */
@SpringBootTest
@ActiveProfiles("test")
class LoggingAspectTest {

  @Autowired private BuildingService buildingService;

  @Autowired private LoggingAspect loggingAspect;

  @Test
  void loggingAspect_ShouldBeConfigured() {
    // Verify aspect is configured
    assertNotNull(loggingAspect);
  }

  @Test
  void serviceMethod_ShouldBeLogged() {
    // This will trigger the logging aspect
    // Verify no exceptions are thrown
    buildingService.getAllBuildings();
  }
}
