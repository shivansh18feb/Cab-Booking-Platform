# ==========================================
# Build Stage
# ==========================================
FROM maven:3.9-eclipse-temurin-21-alpine AS builder

WORKDIR /build

# Cache dependency layer
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source and package
COPY src ./src
RUN mvn clean package -DskipTests -B

# ==========================================
# Production Runtime Stage
# ==========================================
FROM eclipse-temurin:21-jre-alpine

LABEL maintainer="CabFlow Platform Engineering <devops@cabflow.app>"
LABEL description="CabFlow Production Backend Service"

# Security: Create non-privileged user and group
RUN addgroup -S cabgroup && adduser -S cabuser -G cabgroup

WORKDIR /app

# Copy executable jar from builder
COPY --from=builder /build/target/*.jar app.jar
RUN chown -R cabuser:cabgroup /app

USER cabuser

EXPOSE 8080

# Health check using Spring Actuator
HEALTHCHECK --interval=30s --timeout=5s --start-period=40s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1

ENV JAVA_OPTS="-XX:+UseG1GC -XX:MaxRAMPercentage=75.0 -Djava.security.egd=file:/dev/./urandom"
ENV SPRING_PROFILES_ACTIVE="prod"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
