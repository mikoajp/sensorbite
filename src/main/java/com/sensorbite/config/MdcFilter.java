/* Copyright (c) 2024 Sensorbite. All rights reserved. */
package com.sensorbite.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Filter that adds request tracking information to MDC (Mapped Diagnostic Context). This allows
 * correlation of all log entries for a single request.
 */
@Component
@Order(1)
@Slf4j
public class MdcFilter extends OncePerRequestFilter {

  private static final String REQUEST_ID_HEADER = "X-Request-Id";
  private static final String REQUEST_ID_KEY = "requestId";
  private static final String METHOD_KEY = "method";
  private static final String URI_KEY = "uri";
  private static final String CLIENT_IP_KEY = "clientIp";

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {

    try {
      // Get or generate request ID
      String requestId = request.getHeader(REQUEST_ID_HEADER);
      if (requestId == null || requestId.isEmpty()) {
        requestId = UUID.randomUUID().toString();
      }

      // Add request context to MDC
      MDC.put(REQUEST_ID_KEY, requestId);
      MDC.put(METHOD_KEY, request.getMethod());
      MDC.put(URI_KEY, request.getRequestURI());
      MDC.put(CLIENT_IP_KEY, getClientIp(request));

      // Add request ID to response headers for client tracking
      response.addHeader(REQUEST_ID_HEADER, requestId);

      log.debug(
          "Request started: {} {} [{}]", request.getMethod(), request.getRequestURI(), requestId);

      long startTime = System.currentTimeMillis();

      try {
        filterChain.doFilter(request, response);
      } finally {
        long duration = System.currentTimeMillis() - startTime;
        log.debug(
            "Request completed: {} {} in {}ms [{}]",
            request.getMethod(),
            request.getRequestURI(),
            duration,
            requestId);
      }

    } finally {
      // Always clear MDC to prevent memory leaks
      MDC.clear();
    }
  }

  /**
   * Extracts the client IP address from the request. Handles proxy headers (X-Forwarded-For,
   * X-Real-IP).
   */
  private String getClientIp(HttpServletRequest request) {
    String ip = request.getHeader("X-Forwarded-For");

    if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
      ip = request.getHeader("X-Real-IP");
    }

    if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
      ip = request.getRemoteAddr();
    }

    // If multiple IPs in X-Forwarded-For, get the first one
    if (ip != null && ip.contains(",")) {
      ip = ip.split(",")[0].trim();
    }

    return ip != null ? ip : "unknown";
  }
}
