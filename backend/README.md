# Veyru Backend

Spring Boot API for Veyru. See the [project README](../README.md) for the full-stack quick start and architecture.

## Stack

- Java 25 and Spring Boot 4.1
- MongoDB, Redis and Neo4j
- Spring Security, OAuth2 and HttpOnly cookie sessions
- REST/OpenAPI and STOMP WebSocket
- Cloudinary image storage and SMTP email integrations

## Local development

From this directory:

```bash
task infra
cp ../.env.example .env
SPRING_PROFILES_ACTIVE=local ./mvnw spring-boot:run
```

`task infra` starts MongoDB, Redis and Neo4j from the root Compose file. Cloudinary, Google OAuth and SMTP require real values in `.env`; the local placeholders only allow the application to start.

## Checks

```bash
./mvnw spotless:check test
docker build -t veyru-backend .
```

Swagger UI is available at http://localhost:8080/swagger-ui.html. The committed API contract is [`openapi/openapi.json`](openapi/openapi.json).
