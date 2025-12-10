/* Copyright (c) 2024 Sensorbite. All rights reserved. */
package com.sensorbite.evacuation.config;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.retry.RetryRegistry;
import java.time.Duration;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Configuration for Resilience4j circuit breaker and retry patterns. */
@Configuration
@Slf4j
public class ResilienceConfig {

  /** Circuit breaker for Sentinel Hub API calls. */
  @Bean
  public CircuitBreaker sentinelHubCircuitBreaker() {
    CircuitBreakerConfig config =
        CircuitBreakerConfig.custom()
            .failureRateThreshold(50) // Open circuit if 50% of calls fail
            .waitDurationInOpenState(Duration.ofSeconds(60)) // Wait 60s before half-open
            .permittedNumberOfCallsInHalfOpenState(3) // Try 3 calls in half-open state
            .slidingWindowSize(10) // Measure over last 10 calls
            .recordExceptions(Exception.class)
            .build();

    CircuitBreakerRegistry registry = CircuitBreakerRegistry.of(config);
    CircuitBreaker circuitBreaker = registry.circuitBreaker("sentinelHub");

    // Log state transitions
    circuitBreaker
        .getEventPublisher()
        .onStateTransition(
            event ->
                log.warn(
                    "Circuit breaker state transition: {} -> {}",
                    event.getStateTransition().getFromState(),
                    event.getStateTransition().getToState()))
        .onError(
            event ->
                log.error("Circuit breaker recorded error: {}", event.getThrowable().getMessage()));

    return circuitBreaker;
  }

  /** Retry configuration for Sentinel Hub API calls. */
  @Bean
  public Retry sentinelHubRetry() {
    RetryConfig config =
        RetryConfig.custom()
            .maxAttempts(3) // Max 3 attempts
            .waitDuration(Duration.ofSeconds(1)) // Wait 1s between attempts
            .retryExceptions(Exception.class)
            .build();

    RetryRegistry registry = RetryRegistry.of(config);
    Retry retry = registry.retry("sentinelHub");

    // Log retry attempts
    retry
        .getEventPublisher()
        .onRetry(
            event ->
                log.warn(
                    "Retry attempt {} for Sentinel Hub API", event.getNumberOfRetryAttempts()));

    return retry;
  }
}
