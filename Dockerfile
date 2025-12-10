# Multi-stage Docker build for Sensorbite application

# Stage 1: Build
FROM gradle:9.2-jdk17 AS builder

WORKDIR /app

# Copy Gradle files first for better layer caching
COPY build.gradle settings.gradle ./
COPY gradle ./gradle

# Download dependencies (cached if no changes to build files)
RUN gradle dependencies --no-daemon || return 0

# Copy source code
COPY src ./src

# Build the application
RUN gradle clean build --no-daemon -x test

# Stage 2: Runtime
FROM eclipse-temurin:17-jre-alpine

LABEL maintainer="sensorbite@example.com"
LABEL description="Sensorbite - Evacuation Route Planning System"

# Create app user for security
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

# Set working directory
WORKDIR /app

# Copy JAR from builder stage
COPY --from=builder /app/build/libs/*.jar app.jar

# Copy sample data
COPY --from=builder /app/src/main/resources/data ./data

# Create logs directory
RUN mkdir -p logs && chown -R appuser:appgroup /app

# Switch to non-root user
USER appuser

# Expose port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1

# JVM options for container environment
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -XX:InitialRAMPercentage=50.0"

# Run application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
