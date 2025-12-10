/* Copyright (c) 2024 Sensorbite. All rights reserved. */
package com.sensorbite.evacuation.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Configuration for caching flood zone data and road networks. */
@Configuration
@EnableCaching
@RequiredArgsConstructor
public class CacheConfig {

  private final SentinelHubProperties sentinelHubProperties;

  /** Configures Caffeine cache manager for flood zones and road networks. */
  @Bean
  public CacheManager cacheManager() {
    CaffeineCacheManager cacheManager = new CaffeineCacheManager("floodZones", "roadNetworks");

    cacheManager.setCaffeine(
        Caffeine.newBuilder()
            .maximumSize(100) // Maximum 100 entries
            .expireAfterWrite(Duration.ofMinutes(sentinelHubProperties.getCacheTtlMinutes()))
            .recordStats()); // Enable statistics

    return cacheManager;
  }
}
