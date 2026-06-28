# =============================================================================
# Multi-stage Dockerfile for Spring Boot Banking Microservices
# Usage:
#   docker build --build-arg SERVICE_NAME=customer-service -t customer-service .
# =============================================================================

# ── Stage 1: Build ──────────────────────────────────────────────────────────
FROM maven:3.9-eclipse-temurin-21 AS builder

WORKDIR /app

# Copy Maven settings for better repository management
COPY maven-settings.xml /root/.m2/settings.xml
ENV MAVEN_OPTS="-Dmaven.repo.local=/root/.m2/repository"

# ── Dependency caching: copy only POMs first for layer caching ──
COPY pom.xml .
COPY config-server/pom.xml      ./config-server/
COPY discovery-server/pom.xml   ./discovery-server/
COPY api-gateway/pom.xml        ./api-gateway/
COPY customer-service/pom.xml   ./customer-service/
COPY account-service/pom.xml    ./account-service/
COPY transaction-service/pom.xml ./transaction-service/
COPY loan-service/pom.xml       ./loan-service/
COPY card-service/pom.xml       ./card-service/

# Download all dependencies (cached as a separate layer when POMs don't change)
RUN mvn dependency:go-offline -B \
    --settings /root/.m2/settings.xml \
    -Dorg.slf4j.simpleLogger.defaultLogLevel=WARN \
    || true

# ── Copy full source and build ──────────────────────────────────────────────
COPY config-server/      ./config-server/
COPY discovery-server/   ./discovery-server/
COPY api-gateway/        ./api-gateway/
COPY customer-service/   ./customer-service/
COPY account-service/    ./account-service/
COPY transaction-service/ ./transaction-service/
COPY loan-service/       ./loan-service/
COPY card-service/       ./card-service/

ARG SERVICE_NAME
RUN mvn clean package -pl ${SERVICE_NAME} -am -DskipTests -B \
    --settings /root/.m2/settings.xml \
    -Dorg.slf4j.simpleLogger.defaultLogLevel=INFO

# ── Stage 2: Runtime ────────────────────────────────────────────────────────
FROM eclipse-temurin:21-jre-alpine

LABEL maintainer="banking-team"
LABEL org.opencontainers.image.description="Banking Microservice"

WORKDIR /app

# Create a non-root user for security
RUN addgroup -g 1000 spring && \
    adduser -D -u 1000 -G spring spring

# Copy the built JAR (with correct ownership in one step — no extra layer)
ARG SERVICE_NAME
COPY --from=builder --chown=spring:spring \
    /app/${SERVICE_NAME}/target/${SERVICE_NAME}-0.0.1-SNAPSHOT.jar app.jar

USER spring

# Actuator health port (same as app port — Spring Boot default)
EXPOSE 8080

# Health check using the Spring Boot Actuator endpoint
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD sh -c 'wget -qO- "http://localhost:${SERVER_PORT:-8080}/actuator/health" | grep -q '"'"'"status":"UP"'"'"' || exit 1'

# JVM tuning: container-aware heap sizing + GC logging
ENTRYPOINT ["java", \
    "-XX:+UseContainerSupport", \
    "-XX:MaxRAMPercentage=75.0", \
    "-XX:+UseG1GC", \
    "-Djava.security.egd=file:/dev/./urandom", \
    "-jar", "app.jar"]
