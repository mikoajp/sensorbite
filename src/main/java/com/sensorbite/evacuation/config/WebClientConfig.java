/* Copyright (c) 2024 Sensorbite. All rights reserved. */
package com.sensorbite.evacuation.config;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import java.time.Duration;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

/** Configuration for WebClient used in external API calls. */
@Configuration
@RequiredArgsConstructor
@Slf4j
public class WebClientConfig {

  private final SentinelHubProperties sentinelHubProperties;

  /** Creates a WebClient configured for Sentinel Hub API calls. */
  @Bean
  public WebClient sentinelHubWebClient() {
    HttpClient httpClient =
        HttpClient.create()
            .option(
                ChannelOption.CONNECT_TIMEOUT_MILLIS,
                sentinelHubProperties.getTimeoutSeconds() * 1000)
            .responseTimeout(Duration.ofSeconds(sentinelHubProperties.getTimeoutSeconds()))
            .doOnConnected(
                conn ->
                    conn.addHandlerLast(
                            new ReadTimeoutHandler(
                                sentinelHubProperties.getTimeoutSeconds(), TimeUnit.SECONDS))
                        .addHandlerLast(
                            new WriteTimeoutHandler(
                                sentinelHubProperties.getTimeoutSeconds(), TimeUnit.SECONDS)));

    return WebClient.builder()
        .baseUrl(sentinelHubProperties.getBaseUrl())
        .clientConnector(new ReactorClientHttpConnector(httpClient))
        .filter(logRequest())
        .filter(logResponse())
        .build();
  }

  /** Logs outgoing requests. */
  private ExchangeFilterFunction logRequest() {
    return ExchangeFilterFunction.ofRequestProcessor(
        clientRequest -> {
          if (log.isDebugEnabled()) {
            log.debug("Request: {} {}", clientRequest.method(), clientRequest.url());
            clientRequest
                .headers()
                .forEach(
                    (name, values) ->
                        values.forEach(value -> log.debug("Header: {}={}", name, value)));
          }
          return Mono.just(clientRequest);
        });
  }

  /** Logs incoming responses. */
  private ExchangeFilterFunction logResponse() {
    return ExchangeFilterFunction.ofResponseProcessor(
        clientResponse -> {
          if (log.isDebugEnabled()) {
            log.debug(
                "Response: {} {}",
                clientResponse.statusCode(),
                clientResponse.statusCode().value());
          }
          return Mono.just(clientResponse);
        });
  }
}
