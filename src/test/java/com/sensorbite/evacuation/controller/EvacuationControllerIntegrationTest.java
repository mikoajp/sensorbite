/* Copyright (c) 2024 Sensorbite. All rights reserved. */
package com.sensorbite.evacuation.controller;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class EvacuationControllerIntegrationTest {

  @Autowired private MockMvc mockMvc;

  @Test
  void health_ShouldReturnOk() throws Exception {
    mockMvc
        .perform(get("/api/evac/health"))
        .andExpect(status().isOk())
        .andExpect(content().string(containsString("operational")));
  }

  @Test
  void calculateRoute_WithValidCoordinates_ShouldReturnGeoJSON() throws Exception {
    mockMvc
        .perform(
            get("/api/evac/route")
                .param("startLat", "52.0")
                .param("startLon", "21.0")
                .param("endLat", "52.04")
                .param("endLon", "21.04"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.type", is("FeatureCollection")))
        .andExpect(jsonPath("$.features", hasSize(1)))
        .andExpect(jsonPath("$.features[0].type", is("Feature")))
        .andExpect(jsonPath("$.features[0].geometry.type", is("LineString")))
        .andExpect(jsonPath("$.features[0].properties.distance_km").exists())
        .andExpect(jsonPath("$.features[0].properties.estimated_time_minutes").exists())
        .andExpect(jsonPath("$.features[0].properties.safety_score").exists())
        .andExpect(jsonPath("$.metadata.calculation_time_ms").exists())
        .andExpect(jsonPath("$.metadata.algorithm", is("modified_dijkstra")));
  }

  @Test
  void calculateRoute_WithMissingStartLat_ShouldReturnBadRequest() throws Exception {
    mockMvc
        .perform(
            get("/api/evac/route")
                .param("startLon", "21.0")
                .param("endLat", "52.04")
                .param("endLon", "21.04"))
        .andExpect(status().isBadRequest());
  }

  @Test
  void calculateRoute_WithInvalidLatitude_ShouldReturnBadRequest() throws Exception {
    mockMvc
        .perform(
            get("/api/evac/route")
                .param("startLat", "95.0") // Invalid latitude
                .param("startLon", "21.0")
                .param("endLat", "52.04")
                .param("endLon", "21.04"))
        .andExpect(status().isBadRequest());
  }

  @Test
  void calculateRoute_WithSameStartAndEnd_ShouldReturnBadRequest() throws Exception {
    mockMvc
        .perform(
            get("/api/evac/route")
                .param("startLat", "52.0")
                .param("startLon", "21.0")
                .param("endLat", "52.0")
                .param("endLon", "21.0"))
        .andExpect(status().isBadRequest());
  }

  @Test
  void calculateRoute_WithInvalidLongitude_ShouldReturnBadRequest() throws Exception {
    mockMvc
        .perform(
            get("/api/evac/route")
                .param("startLat", "52.0")
                .param("startLon", "200.0") // Invalid longitude
                .param("endLat", "52.04")
                .param("endLon", "21.04"))
        .andExpect(status().isBadRequest());
  }
}
