/* Copyright (c) 2024 Sensorbite. All rights reserved. */
package com.sensorbite.config;

import java.util.Arrays;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

/**
 * Aspect for logging method executions with performance metrics. Provides automatic logging for
 * service layer methods.
 */
@Aspect
@Component
@Slf4j
public class LoggingAspect {

  /** Logs all service method calls with execution time. */
  @Around("execution(* com.sensorbite..service..*(..))")
  public Object logServiceMethods(ProceedingJoinPoint joinPoint) throws Throwable {
    MethodSignature signature = (MethodSignature) joinPoint.getSignature();
    String className = signature.getDeclaringType().getSimpleName();
    String methodName = signature.getName();

    long startTime = System.currentTimeMillis();

    log.debug(
        "Entering {}.{}() with arguments: {}",
        className,
        methodName,
        Arrays.toString(joinPoint.getArgs()));

    try {
      Object result = joinPoint.proceed();

      long executionTime = System.currentTimeMillis() - startTime;

      log.debug("Exiting {}.{}() in {}ms", className, methodName, executionTime);

      // Log slow methods
      if (executionTime > 1000) {
        log.warn("Slow method execution: {}.{}() took {}ms", className, methodName, executionTime);
      }

      return result;

    } catch (Exception e) {
      long executionTime = System.currentTimeMillis() - startTime;
      log.error(
          "Exception in {}.{}() after {}ms: {}",
          className,
          methodName,
          executionTime,
          e.getMessage(),
          e);
      throw e;
    }
  }

  /** Logs all controller method calls with request details. */
  @Around("execution(* com.sensorbite..controller..*(..))")
  public Object logControllerMethods(ProceedingJoinPoint joinPoint) throws Throwable {
    MethodSignature signature = (MethodSignature) joinPoint.getSignature();
    String className = signature.getDeclaringType().getSimpleName();
    String methodName = signature.getName();

    long startTime = System.currentTimeMillis();

    log.info("API Request: {}.{}()", className, methodName);

    try {
      Object result = joinPoint.proceed();

      long executionTime = System.currentTimeMillis() - startTime;

      log.info("API Response: {}.{}() completed in {}ms", className, methodName, executionTime);

      return result;

    } catch (Exception e) {
      long executionTime = System.currentTimeMillis() - startTime;
      log.error(
          "API Error: {}.{}() failed after {}ms: {}",
          className,
          methodName,
          executionTime,
          e.getMessage());
      throw e;
    }
  }

  /** Logs all repository method calls for database operations. */
  @Around("execution(* com.sensorbite..repository..*(..))")
  public Object logRepositoryMethods(ProceedingJoinPoint joinPoint) throws Throwable {
    MethodSignature signature = (MethodSignature) joinPoint.getSignature();
    String methodName = signature.getName();

    long startTime = System.currentTimeMillis();

    try {
      Object result = joinPoint.proceed();

      long executionTime = System.currentTimeMillis() - startTime;

      // Log slow database queries
      if (executionTime > 500) {
        log.warn("Slow database query: {}() took {}ms", methodName, executionTime);
      } else {
        log.trace("Database query: {}() completed in {}ms", methodName, executionTime);
      }

      return result;

    } catch (Exception e) {
      long executionTime = System.currentTimeMillis() - startTime;
      log.error("Database error in {}() after {}ms: {}", methodName, executionTime, e.getMessage());
      throw e;
    }
  }
}
