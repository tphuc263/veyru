package com.veyru.config;

import java.net.URI;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
@Profile("prod")
public class ProductionConfigurationValidator implements InitializingBean {
  static final List<String> REQUIRED_PROPERTIES =
      List.of(
          "spring.mongodb.uri",
          "spring.data.redis.url",
          "spring.neo4j.uri",
          "spring.neo4j.authentication.username",
          "spring.neo4j.authentication.password",
          "auth.token.jwt-secret",
          "cors.allowed-origins",
          "app.frontend.url",
          "app.oauth2.redirect-uri",
          "app.oauth2.failure-redirect-uri",
          "open.api.server-url",
          "cloudinary.cloud-name",
          "cloudinary.api-key",
          "cloudinary.api-secret",
          "spring.security.oauth2.client.registration.google.client-id",
          "spring.security.oauth2.client.registration.google.client-secret",
          "spring.mail.host",
          "spring.mail.port",
          "spring.mail.username",
          "spring.mail.password");
  private static final Set<String> SECURE_NEO4J_SCHEMES =
      Set.of("neo4j+s", "neo4j+ssc", "bolt+s", "bolt+ssc");

  private final AuthProperties auth;
  private final ApplicationProperties application;
  private final CorsProperties cors;
  private final OpenApiProperties openApi;
  private final Environment environment;

  @Override
  public void afterPropertiesSet() {
    REQUIRED_PROPERTIES.forEach(this::requireResolvedProperty);

    if (!auth.cookie().secure()) {
      throw new IllegalStateException("Production session cookies must be secure");
    }

    requirePublicHttps(application.frontend().url(), "app.frontend.url");
    requirePublicHttps(application.oauth2().redirectUri(), "app.oauth2.redirect-uri");
    requirePublicHttps(
        application.oauth2().failureRedirectUri(), "app.oauth2.failure-redirect-uri");
    requirePublicHttps(openApi.serverUrl(), "open.api.server-url");
    cors.allowedOrigins()
        .forEach(origin -> requirePublicHttps(URI.create(origin), "cors.allowed-origins"));

    requireMongoUri(requireResolvedProperty("spring.mongodb.uri"));
    requireConnectionUri(
        requireResolvedProperty("spring.data.redis.url"),
        "spring.data.redis.url",
        Set.of("redis", "rediss"));
    requireConnectionUri(
        requireResolvedProperty("spring.neo4j.uri"), "spring.neo4j.uri", SECURE_NEO4J_SCHEMES);
  }

  private String requireResolvedProperty(String propertyName) {
    try {
      String value = environment.getProperty(propertyName);
      if (!StringUtils.hasText(value) || value.contains("${")) {
        throw missing(propertyName);
      }
      return value;
    } catch (IllegalArgumentException exception) {
      throw missing(propertyName, exception);
    }
  }

  private static void requireMongoUri(String value) {
    URI uri = requireConnectionUri(value, "spring.mongodb.uri", Set.of("mongodb", "mongodb+srv"));
    if ("mongodb".equalsIgnoreCase(uri.getScheme())) {
      String query = uri.getQuery();
      String normalizedQuery = query == null ? "" : query.toLowerCase(Locale.ROOT);
      if (!normalizedQuery.contains("tls=true") && !normalizedQuery.contains("ssl=true")) {
        throw new IllegalStateException(
            "Production property spring.mongodb.uri must enable TLS or use mongodb+srv");
      }
    }
  }

  private static URI requireConnectionUri(
      String value, String propertyName, Set<String> allowedSchemes) {
    try {
      URI uri = URI.create(value);
      String scheme = uri.getScheme() == null ? "" : uri.getScheme().toLowerCase(Locale.ROOT);
      if (!allowedSchemes.contains(scheme) || uri.getHost() == null || isLocalHost(uri.getHost())) {
        throw invalid(propertyName);
      }
      return uri;
    } catch (IllegalArgumentException exception) {
      throw invalid(propertyName, exception);
    }
  }

  private static void requirePublicHttps(URI uri, String propertyName) {
    if (uri == null
        || !"https".equalsIgnoreCase(uri.getScheme())
        || uri.getHost() == null
        || isLocalHost(uri.getHost())) {
      throw invalid(propertyName);
    }
  }

  private static boolean isLocalHost(String host) {
    String normalized = host.toLowerCase(Locale.ROOT);
    return "localhost".equals(normalized)
        || normalized.endsWith(".localhost")
        || "0.0.0.0".equals(normalized)
        || "::1".equals(normalized)
        || normalized.startsWith("127.");
  }

  private static IllegalStateException invalid(String propertyName) {
    return new IllegalStateException("Production property " + propertyName + " is not secure");
  }

  private static IllegalStateException invalid(String propertyName, RuntimeException cause) {
    return new IllegalStateException(
        "Production property " + propertyName + " is not secure", cause);
  }

  private static IllegalStateException missing(String propertyName) {
    return new IllegalStateException(
        "Required production property " + propertyName + " is missing");
  }

  private static IllegalStateException missing(String propertyName, RuntimeException cause) {
    return new IllegalStateException(
        "Required production property " + propertyName + " is missing", cause);
  }

  public ProductionConfigurationValidator(
      AuthProperties auth,
      ApplicationProperties application,
      CorsProperties cors,
      OpenApiProperties openApi,
      Environment environment) {
    this.auth = auth;
    this.application = application;
    this.cors = cors;
    this.openApi = openApi;
    this.environment = environment;
  }
}
