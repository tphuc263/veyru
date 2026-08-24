package com.veyru;

import static org.assertj.core.api.Assertions.assertThat;

import com.veyru.application.identity.AuthenticatedUser;
import com.veyru.application.port.out.AuthorizationCodeStore;
import com.veyru.application.port.out.RefreshSessionStore;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.containers.Neo4jContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import tools.jackson.databind.ObjectMapper;

@Testcontainers
@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = {
      "cloudinary.cloudName=test",
      "cloudinary.apiKey=test",
      "cloudinary.apiSecret=test",
      "auth.token.jwtSecret=VGhpcy1pcy1hLXRlc3Qtc2VjcmV0LWtleS0zMi1ieXRlcw==",
      "auth.cookie.secure=false",
      "spring.security.oauth2.client.registration.google.client-id=test",
      "spring.security.oauth2.client.registration.google.client-secret=test",
      "spring.mail.host=localhost",
      "spring.mail.port=2525"
    })
class VeyruApplicationTests {
  @Container @ServiceConnection
  static final MongoDBContainer mongo = new MongoDBContainer("mongo:7.0");

  @Container
  @ServiceConnection(name = "redis")
  static final GenericContainer<?> redis =
      new GenericContainer<>(DockerImageName.parse("redis/redis-stack-server:7.4.0-v8"))
          .withExposedPorts(6379);

  @Container
  static final Neo4jContainer<?> neo4j =
      new Neo4jContainer<>("neo4j:5.26.29").withAdminPassword("test-password");

  @DynamicPropertySource
  static void neo4jProperties(DynamicPropertyRegistry properties) {
    properties.add("NEO4J_URI", neo4j::getBoltUrl);
    properties.add("NEO4J_USERNAME", () -> "neo4j");
    properties.add("NEO4J_PASSWORD", () -> "test-password");
  }

  @Autowired AuthorizationCodeStore authorizationCodes;
  @Autowired RefreshSessionStore refreshSessions;
  @Autowired ObjectMapper objectMapper;
  @LocalServerPort int port;

  private final AuthenticatedUser user =
      new AuthenticatedUser("user-1", "alice", "alice@example.com", "ROLE_USER");

  @Test
  void contextLoads() {}

  @Test
  void livenessIsAvailableWithoutExternalProductionServices() throws Exception {
    var response = get("/actuator/health/liveness");

    assertThat(response.statusCode()).isEqualTo(200);
  }

  @Test
  void generatedOpenApiMatchesCommittedSnapshotSemantically() throws Exception {
    var response = get("/v3/api-docs");

    assertThat(response.statusCode()).isEqualTo(200);
    assertThat(objectMapper.readTree(response.body()))
        .isEqualTo(objectMapper.readTree(Files.readString(Path.of("openapi/openapi.json"))));
  }

  @Test
  void oauthCodeIsConsumedExactlyOnce() {
    String code = authorizationCodes.issue(user, Duration.ofMinutes(1));

    assertThat(authorizationCodes.consume(code)).contains(user);
    assertThat(authorizationCodes.consume(code)).isEmpty();
  }

  @Test
  void reusedRefreshTokenRevokesItsRotatedFamily() {
    var created = refreshSessions.create(user, Duration.ofMinutes(1));
    var rotated = refreshSessions.rotate(created.token(), Duration.ofMinutes(1)).orElseThrow();

    assertThat(refreshSessions.rotate(created.token(), Duration.ofMinutes(1))).isEmpty();
    assertThat(refreshSessions.rotate(rotated.token(), Duration.ofMinutes(1))).isEmpty();
  }

  private HttpResponse<String> get(String path) throws Exception {
    return HttpClient.newHttpClient()
        .send(
            HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + port + path)).GET().build(),
            HttpResponse.BodyHandlers.ofString());
  }
}
