# syntax=docker/dockerfile:1

FROM eclipse-temurin:21-jdk AS builder
WORKDIR /app

# Cache Gradle wrapper and dependencies first
COPY gradlew settings.gradle build.gradle /app/
COPY gradle /app/gradle
RUN ./gradlew --no-daemon --version

# Copy sources and build
COPY src /app/src
RUN ./gradlew --no-daemon bootJar

FROM eclipse-temurin:21-jre
WORKDIR /app

# Create non-root user
RUN useradd -u 10001 -m appuser

COPY --from=builder /app/build/libs/*.jar /app/app.jar

USER appuser
EXPOSE 8080

ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]
