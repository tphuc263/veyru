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

`task infra` starts MongoDB, Redis and Neo4j from the root Compose file.

Before starting the backend, fill in the required Cloudinary, Google OAuth and SMTP values in
`backend/.env`. The application fails during startup and names the missing property when any of
these dependencies is not configured.

Cloudinary uses `CLOUDINARY_CLOUD_NAME`, `CLOUDINARY_API_KEY`, and `CLOUDINARY_API_SECRET`. Google OAuth and mail use Spring Boot's canonical environment names shown in `.env.example`, such as `SPRING_SECURITY_OAUTH2_CLIENT_REGISTRATION_GOOGLE_CLIENTID` and `SPRING_MAIL_USERNAME`. Spring removes dashes when converting canonical property names to environment-variable names.

Existing local files must rename `GOOGLE_CLIENT_ID`/`GOOGLE_CLIENT_SECRET` and `MAIL_*` to their
`SPRING_SECURITY_OAUTH2_*` and `SPRING_MAIL_*` equivalents. Secret values themselves do not change.

`backend/.env` is read only by the `local` profile. A public deployment stores values in the
hosting platform's environment-variable or secret settings and does not deploy a `.env` file.

Production also requires `MONGODB_URI`, `NEO4J_PASSWORD`, `JWT_SECRET`, `CORS_ALLOWED_ORIGINS`, `FRONTEND_URL`, `OAUTH2_REDIRECT_URI`, `OAUTH2_FAILURE_REDIRECT_URI`, and `OPENAPI_SERVER_URL`. `JWT_SECRET` must be Base64-encoded key material of at least 32 bytes.

Configuration priority for normal deployment is: packaged YAML, profile-specific YAML, OS environment variables, Java system properties, then command-line arguments. Custom settings use immutable `@ConfigurationProperties` records and are validated during startup.

## Checks

```bash
./mvnw spotless:check test
docker build -t veyru-backend .
```

Swagger UI is available at http://localhost:8080/swagger-ui.html. The committed API contract is [`openapi/openapi.json`](openapi/openapi.json).
