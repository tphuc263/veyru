# Multi-stage build for Spring Boot Application
FROM maven:3.9.9-eclipse-temurin-21 AS build

WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -B
COPY src ./src
RUN mvn clean package -DskipTests

# Runtime stage
FROM eclipse-temurin:21-jre
WORKDIR /app

# Cài đặt wget cho healthcheck (alpine-based images thì dùng apk, debian-based dùng apt-get)
# eclipse-temurin là debian-based
RUN apt-get update && \
    apt-get install -y wget && \
    rm -rf /var/lib/apt/lists/*

RUN groupadd -r spring && useradd -r -g spring spring
COPY --from=build /app/target/*.jar app.jar
RUN mkdir -p /app/logs && chown -R spring:spring /app

USER spring
EXPOSE 8080 9092

# Healthcheck đã được move sang docker-compose.yml để dễ config hơn
# Nhưng giữ lại ở đây để khi chạy standalone vẫn có
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=5 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8080/api/v1/health || exit 1

ENTRYPOINT ["java", "-jar", "/app/app.jar"]