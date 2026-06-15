# Multi-stage build for Spring Boot Microservices
# This Dockerfile can be used for all microservices

# Stage 1: Build
FROM maven:3.9-eclipse-temurin-21 AS builder

WORKDIR /app

# Copy Maven settings for better repository management
COPY maven-settings.xml /root/.m2/settings.xml

# Copy parent pom.xml
COPY pom.xml .

# Copy all service modules
COPY config-server ./config-server
COPY discovery-server ./discovery-server
COPY api-gateway ./api-gateway
COPY customer-service ./customer-service
COPY account-service ./account-service
COPY transaction-service ./transaction-service
COPY loan-service ./loan-service
COPY card-service ./card-service

# Build all modules. We don't use offline mode so Maven will download dependencies
RUN mvn clean package -DskipTests \
    --settings /root/.m2/settings.xml \
    -Dorg.slf4j.simpleLogger.defaultLogLevel=INFO

# Stage 2: Runtime
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Create a non-root user for security
RUN addgroup -g 1000 spring && \
    adduser -D -u 1000 -G spring spring

# Copy the JAR from builder
ARG SERVICE_NAME
COPY --from=builder /app/${SERVICE_NAME}/target/${SERVICE_NAME}-0.0.1-SNAPSHOT.jar app.jar

# Change ownership to spring user
RUN chown -R spring:spring /app

USER spring

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
