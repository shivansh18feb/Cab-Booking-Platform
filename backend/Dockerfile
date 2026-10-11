
# Build Stage
FROM maven:3.9-eclipse-temurin-21-alpine AS builder

WORKDIR /build

# Configure a mirror for Maven Central
RUN mkdir -p /root/.m2 && \
    printf '%s\n' \
    '<settings><mirrors><mirror><id>google-central</id><name>Google Maven Central Mirror</name><url>https://maven-central.storage-download.googleapis.com/maven2/</url><mirrorOf>central</mirrorOf></mirror></mirrors></settings>' \
    > /root/.m2/settings.xml

# Copy Maven project files
COPY pom.xml .
COPY src ./src

# Build the application
RUN mvn -B -ntp clean package -DskipTests

# Production Runtime Stage
FROM eclipse-temurin:21-jre-alpine

LABEL maintainer="CabFlow Platform Engineering"
LABEL description="CabFlow Production Backend Service"

RUN addgroup -S cabgroup && adduser -S cabuser -G cabgroup

WORKDIR /app

COPY --from=builder /build/target/*.jar app.jar
RUN chown -R cabuser:cabgroup /app

USER cabuser

EXPOSE 8080

ENV JAVA_OPTS="-XX:+UseG1GC -XX:MaxRAMPercentage=75.0 -Djava.security.egd=file:/dev/./urandom"
ENV SPRING_PROFILES_ACTIVE="prod"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
