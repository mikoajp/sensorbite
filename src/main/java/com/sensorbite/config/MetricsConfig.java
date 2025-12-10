/* Copyright (c) 2024 Sensorbite. All rights reserved. */
package com.sensorbite.config;

import io.micrometer.core.aop.TimedAspect;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for application metrics using Micrometer. Exposes metrics for Prometheus and other
 * monitoring systems.
 */
@Configuration
@Slf4j
public class MetricsConfig {

  /** Enables @Timed annotation support for method execution timing. */
  @Bean
  public TimedAspect timedAspect(MeterRegistry registry) {
    return new TimedAspect(registry);
  }

  /** Registers custom application metrics. */
  @Bean
  public CustomMetrics customMetrics(MeterRegistry registry) {
    log.info("Registering custom metrics");
    return new CustomMetrics(registry);
  }

  /** Custom metrics holder for application-specific measurements. */
  public static class CustomMetrics {
    private final MeterRegistry registry;
    private final Timer routeCalculationTimer;
    private final Timer hazardZoneFetchTimer;

    public CustomMetrics(MeterRegistry registry) {
      this.registry = registry;

      // Register timers
      this.routeCalculationTimer =
          Timer.builder("sensorbite.route.calculation")
              .description("Time taken to calculate evacuation routes")
              .tag("type", "evacuation")
              .register(registry);

      this.hazardZoneFetchTimer =
          Timer.builder("sensorbite.hazard.fetch")
              .description("Time taken to fetch hazard zones")
              .tag("source", "sentinel-hub")
              .register(registry);

      // Register counters
      registry.counter("sensorbite.route.requests", "status", "total");
      registry.counter("sensorbite.route.requests", "status", "success");
      registry.counter("sensorbite.route.requests", "status", "failed");

      registry.counter("sensorbite.hazard.cache", "result", "hit");
      registry.counter("sensorbite.hazard.cache", "result", "miss");
    }

    public Timer getRouteCalculationTimer() {
      return routeCalculationTimer;
    }

    public Timer getHazardZoneFetchTimer() {
      return hazardZoneFetchTimer;
    }

    public void incrementRouteRequest(String status) {
      registry.counter("sensorbite.route.requests", "status", status).increment();
    }

    public void incrementCacheResult(String result) {
      registry.counter("sensorbite.hazard.cache", "result", result).increment();
    }
  }
}
