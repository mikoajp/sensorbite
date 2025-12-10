/* Copyright (c) 2024 Sensorbite. All rights reserved. */
package com.sensorbite.evacuation.config;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

/** Configuration properties for Sentinel Hub API integration. */
@Configuration
@ConfigurationProperties(prefix = "sentinel-hub")
@Validated
@Data
public class SentinelHubProperties {

  /** Base URL for Sentinel Hub API */
  @NotBlank private String baseUrl = "https://services.sentinel-hub.com";

  /** OAuth2 token endpoint */
  @NotBlank private String tokenUrl = "https://services.sentinel-hub.com/oauth/token";

  /** Client ID for authentication */
  private String clientId;

  /** Client secret for authentication */
  private String clientSecret;

  /** Request timeout in seconds */
  @Positive private int timeoutSeconds = 30;

  /** Maximum number of retry attempts */
  @Positive private int maxRetries = 3;

  /** Enable/disable Sentinel Hub integration */
  private boolean enabled = false;

  /** Mock mode - use sample data instead of real API calls */
  private boolean mockMode = true;

  /** Cache TTL in minutes */
  @Positive private int cacheTtlMinutes = 60;
}
