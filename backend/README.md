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

## Production configuration

Public deployments run with `SPRING_PROFILES_ACTIVE=prod`. The common and production profiles do
not contain localhost infrastructure fallbacks: a missing required value stops startup. Localhost
defaults exist only in `application-local.yml`.

| Variable | Purpose |
| --- | --- |
| `MONGODB_URI` | MongoDB connection URI; production accepts `mongodb+srv` or a TLS-enabled `mongodb` URI |
| `REDIS_URL` | Complete Redis URL including credentials and TLS scheme when required |
| `NEO4J_URI`, `NEO4J_USERNAME`, `NEO4J_PASSWORD` | Encrypted Neo4j connection and credentials |
| `JWT_SECRET` | Base64-encoded random key material of at least 32 bytes |
| `CORS_ALLOWED_ORIGINS` | Exact comma-separated browser origins; production uses `https://veyru.dev` |
| `FRONTEND_URL` | Canonical frontend URL used in application links |
| `OAUTH2_REDIRECT_URI`, `OAUTH2_FAILURE_REDIRECT_URI` | Frontend OAuth completion and failure URLs |
| `OPENAPI_SERVER_URL` | Public API URL, normally `https://api.veyru.dev` |
| `CLOUDINARY_CLOUD_NAME`, `CLOUDINARY_API_KEY`, `CLOUDINARY_API_SECRET` | Image storage credentials |
| `SPRING_SECURITY_OAUTH2_CLIENT_REGISTRATION_GOOGLE_CLIENTID`, `SPRING_SECURITY_OAUTH2_CLIENT_REGISTRATION_GOOGLE_CLIENTSECRET` | Google OAuth client credentials |
| `SPRING_MAIL_HOST`, `SPRING_MAIL_PORT`, `SPRING_MAIL_USERNAME`, `SPRING_MAIL_PASSWORD` | SMTP relay connection |

`PORT` can be supplied by the hosting platform and falls back to 8080 when it is not set.
`AUTH_COOKIE_SECURE` defaults to the secure value `true`. `GRAPH_RECONCILE_ON_STARTUP` defaults to
`false`. These are safe policy defaults rather than missing infrastructure fallbacks.

Vector search is always enabled and requires Redis Stack or another Redis service that supports
`FT.CREATE` and `FT.SEARCH`. Startup fails if the vector index cannot be initialized, so a plain
Redis or Key Value service without Redis Search is not a valid production dependency. A runtime
outage after successful startup still falls back temporarily to tag matching and records the
degraded path in logs and metrics.

The production validator rejects insecure browser URLs, localhost dependencies, insecure Neo4j
URIs and non-secure session cookies. Hosting health checks should use
`/actuator/health/readiness`; `/actuator/health/liveness` intentionally checks only the process.

Google OAuth must authorize this backend callback:

```text
https://api.veyru.dev/login/oauth2/code/google
```

Configuration priority for normal deployment is: packaged YAML, profile-specific YAML, OS environment variables, Java system properties, then command-line arguments. Custom settings use immutable `@ConfigurationProperties` records and are validated during startup.

## Checks

```bash
./mvnw spotless:check test
docker build -t veyru-backend .
```

Swagger UI is available at http://localhost:8080/swagger-ui.html. The committed API contract is [`openapi/openapi.json`](openapi/openapi.json).
