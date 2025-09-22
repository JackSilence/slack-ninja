# Multi-stage build for Spring Boot Slack Ninja application
FROM gradle:8.5-jdk17-alpine AS builder

# Set working directory
WORKDIR /app

# Copy gradle files first for better caching
COPY build.gradle settings.gradle gradlew ./
COPY gradle/ ./gradle/

# Download dependencies (this layer will be cached if dependencies don't change)
RUN ./gradlew dependencies --no-daemon

# Copy source code
COPY src/ ./src/
COPY system.properties ./

# Build the application
RUN ./gradlew bootJar --no-daemon

# Runtime stage
FROM eclipse-temurin:17-jre-alpine

# Install tzdata for timezone support
RUN apk add --no-cache tzdata

# Set timezone to Asia/Taipei (adjust as needed)
ENV TZ=Asia/Taipei

# Create non-root user for security
RUN addgroup -g 1000 ninja && \
    adduser -D -s /bin/sh -u 1000 -G ninja ninja

# Set working directory
WORKDIR /app

# Copy the built jar from builder stage
COPY --from=builder /app/build/libs/*.jar app.jar

# Change ownership to ninja user
RUN chown -R ninja:ninja /app

# Switch to non-root user
USER ninja

# Expose port (Spring Boot default is 8080)
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1

# JVM optimization for container
ENV JAVA_OPTS="-Xmx512m -Xms256m -XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"

# Run the application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
