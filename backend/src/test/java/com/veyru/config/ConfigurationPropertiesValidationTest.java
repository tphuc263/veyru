package com.veyru.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.veyru.application.port.out.ImageStorage;
import java.net.URI;
import java.time.Duration;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.mail.autoconfigure.MailProperties;
import org.springframework.boot.security.oauth2.client.autoconfigure.OAuth2ClientProperties;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.mock.env.MockEnvironment;

class ConfigurationPropertiesValidationTest {
  private static final String VALID_SECRET = "VGhpcy1pcy1hLXRlc3Qtc2VjcmV0LWtleS0zMi1ieXRlcw==";

  @Test
  void bindsValidAuthProperties() {
    new ApplicationContextRunner()
        .withUserConfiguration(AuthPropertiesConfig.class)
        .withPropertyValues(
            "auth.token.jwt-secret=" + VALID_SECRET,
            "auth.token.access-expiration=15m",
            "auth.cookie.secure=true")
        .run(
            context -> {
              assertThat(context).hasNotFailed();
              assertThat(context.getBean(AuthProperties.class).token().accessExpiration())
                  .isEqualTo(Duration.ofMinutes(15));
            });
  }

  @Test
  void rejectsWeakJwtSecretAtStartup() {
    new ApplicationContextRunner()
        .withUserConfiguration(AuthPropertiesConfig.class)
        .withPropertyValues(
            "auth.token.jwt-secret=d2Vhaw==",
            "auth.token.access-expiration=15m",
            "auth.cookie.secure=true")
        .run(context -> assertThat(context).hasFailed());
  }

  @Test
  void rejectsOutOfRangeNewsfeedWeightAtStartup() {
    new ApplicationContextRunner()
        .withUserConfiguration(NewsfeedPropertiesConfig.class)
        .withPropertyValues(
            "newsfeed.cache.affinity-ttl=5m",
            "newsfeed.cache.cursor-ttl=5m",
            "newsfeed.ranking.candidate-limit=200",
            "newsfeed.ranking.lookback-days=30",
            "newsfeed.ranking.graph-weight=1.1",
            "newsfeed.ranking.direct-follow-weight=.4",
            "newsfeed.ranking.interaction-weight=.35",
            "newsfeed.ranking.mutual-weight=.25",
            "newsfeed.ranking.recency-weight=.6",
            "newsfeed.ranking.engagement-weight=.3",
            "newsfeed.ranking.quality-weight=.1",
            "newsfeed.ranking.recency-decay-hours=72",
            "newsfeed.ranking.engagement-scale=50")
        .run(context -> assertThat(context).hasFailed());
  }

  @ParameterizedTest
  @ValueSource(strings = {"cloudinary.cloud-name", "cloudinary.api-key", "cloudinary.api-secret"})
  void cloudinaryRequiresEveryCredential(String omittedProperty) {
    new ApplicationContextRunner()
        .withUserConfiguration(CloudinaryIntegrationConfig.class)
        .withPropertyValues(
            without(
                omittedProperty,
                "cloudinary.cloud-name=test-cloud",
                "cloudinary.api-key=test-key",
                "cloudinary.api-secret=test-secret",
                "cloudinary.secure=true"))
        .run(context -> assertThat(context).hasFailed());
  }

  @Test
  void validCloudinaryConfigurationCreatesImageStorage() {
    new ApplicationContextRunner()
        .withUserConfiguration(CloudinaryIntegrationConfig.class)
        .withPropertyValues(
            "cloudinary.cloud-name=test-cloud",
            "cloudinary.api-key=test-key",
            "cloudinary.api-secret=test-secret",
            "cloudinary.secure=true")
        .run(
            context -> {
              assertThat(context).hasNotFailed();
              assertThat(context).hasSingleBean(ImageStorage.class);
            });
  }

  @ParameterizedTest
  @ValueSource(
      strings = {
        "spring.security.oauth2.client.registration.google.client-id",
        "spring.security.oauth2.client.registration.google.client-secret",
        "spring.mail.host",
        "spring.mail.port",
        "spring.mail.username",
        "spring.mail.password"
      })
  void externalServicesRequireEveryConfigurationValue(String omittedProperty) {
    new ApplicationContextRunner()
        .withUserConfiguration(ExternalServicesConfig.class)
        .withPropertyValues(without(omittedProperty, validExternalServiceProperties()))
        .run(context -> assertThat(context).hasFailed());
  }

  @Test
  void validExternalServiceConfigurationPassesStartupValidation() {
    new ApplicationContextRunner()
        .withUserConfiguration(ExternalServicesConfig.class)
        .withPropertyValues(validExternalServiceProperties())
        .run(context -> assertThat(context).hasNotFailed());
  }

  @Test
  void acceptsSecureProductionConfiguration() {
    assertThat(productionValidator(true, "https://veyru.dev"))
        .satisfies(ProductionConfigurationValidator::afterPropertiesSet);
  }

  @ParameterizedTest
  @ValueSource(
      strings = {
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
        "spring.mail.password"
      })
  void rejectsEachMissingProductionProperty(String omittedProperty) {
    ProductionConfigurationValidator validator =
        productionValidator(true, "https://veyru.dev", productionEnvironment(omittedProperty));

    assertThatThrownBy(validator::afterPropertiesSet)
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining(omittedProperty);
  }

  @Test
  void rejectsUnresolvedProductionPlaceholder() {
    MockEnvironment environment = productionEnvironment();
    environment.setProperty("cloudinary.api-key", "${CLOUDINARY_API_KEY}");

    ProductionConfigurationValidator validator =
        productionValidator(true, "https://veyru.dev", environment);

    assertThatThrownBy(validator::afterPropertiesSet)
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("cloudinary.api-key");
  }

  @Test
  void rejectsInsecureProductionCookie() {
    assertThatThrownBy(() -> productionValidator(false, "https://veyru.dev").afterPropertiesSet())
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("cookies must be secure");
  }

  @ParameterizedTest
  @ValueSource(strings = {"http://veyru.dev", "https://localhost"})
  void rejectsNonPublicProductionUrls(String frontendUrl) {
    assertThatThrownBy(() -> productionValidator(true, frontendUrl).afterPropertiesSet())
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("not secure");
  }

  @Test
  void rejectsLocalProductionInfrastructure() {
    MockEnvironment environment = productionEnvironment();
    environment.setProperty("spring.data.redis.url", "redis://localhost:6379");
    ProductionConfigurationValidator validator =
        productionValidator(true, "https://veyru.dev", environment);

    assertThatThrownBy(validator::afterPropertiesSet)
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("spring.data.redis.url");
  }

  private static String[] validExternalServiceProperties() {
    return new String[] {
      "spring.security.oauth2.client.registration.google.client-id=test-client",
      "spring.security.oauth2.client.registration.google.client-secret=test-secret",
      "spring.mail.host=smtp.example.com",
      "spring.mail.port=587",
      "spring.mail.username=mailer@example.com",
      "spring.mail.password=test-password"
    };
  }

  private static ProductionConfigurationValidator productionValidator(
      boolean secureCookie, String frontendUrl) {
    return productionValidator(secureCookie, frontendUrl, productionEnvironment());
  }

  private static ProductionConfigurationValidator productionValidator(
      boolean secureCookie, String frontendUrl, MockEnvironment environment) {
    URI frontend = URI.create(frontendUrl);
    return new ProductionConfigurationValidator(
        new AuthProperties(
            new AuthProperties.Token(VALID_SECRET, Duration.ofMinutes(15)),
            new AuthProperties.Cookie(secureCookie)),
        new ApplicationProperties(
            new ApplicationProperties.Frontend(frontend),
            new ApplicationProperties.OAuth2(
                frontend.resolve("/auth/oauth2/redirect"), frontend.resolve("/login?error=true"))),
        new CorsProperties(List.of(frontendUrl)),
        new OpenApiProperties(
            "Veyru API",
            "v1.0.0",
            "The flow of moments we see, live, and share.",
            URI.create("https://api.veyru.dev")),
        environment);
  }

  private static MockEnvironment productionEnvironment() {
    return productionEnvironment(null);
  }

  private static MockEnvironment productionEnvironment(String omittedProperty) {
    Map<String, String> values = new LinkedHashMap<>();
    values.put("spring.mongodb.uri", "mongodb+srv://user:password@cluster.example/veyru");
    values.put("spring.data.redis.url", "redis://redis.internal:6379");
    values.put("spring.neo4j.uri", "neo4j+s://graph.example:7687");
    values.put("spring.neo4j.authentication.username", "neo4j");
    values.put("spring.neo4j.authentication.password", "test-password");
    values.put("auth.token.jwt-secret", VALID_SECRET);
    values.put("cors.allowed-origins", "https://veyru.dev");
    values.put("app.frontend.url", "https://veyru.dev");
    values.put("app.oauth2.redirect-uri", "https://veyru.dev/auth/oauth2/redirect");
    values.put("app.oauth2.failure-redirect-uri", "https://veyru.dev/login?error=true");
    values.put("open.api.server-url", "https://api.veyru.dev");
    values.put("cloudinary.cloud-name", "test-cloud");
    values.put("cloudinary.api-key", "test-key");
    values.put("cloudinary.api-secret", "test-secret");
    values.put("spring.security.oauth2.client.registration.google.client-id", "test-client");
    values.put("spring.security.oauth2.client.registration.google.client-secret", "test-secret");
    values.put("spring.mail.host", "smtp.example.com");
    values.put("spring.mail.port", "2525");
    values.put("spring.mail.username", "mailer@example.com");
    values.put("spring.mail.password", "test-password");

    MockEnvironment environment = new MockEnvironment();
    values.entrySet().stream()
        .filter(entry -> !entry.getKey().equals(omittedProperty))
        .forEach(entry -> environment.setProperty(entry.getKey(), entry.getValue()));
    return environment;
  }

  private static String[] without(String omittedProperty, String... propertyValues) {
    return Arrays.stream(propertyValues)
        .filter(value -> !value.startsWith(omittedProperty + "="))
        .toArray(String[]::new);
  }

  @Configuration(proxyBeanMethods = false)
  @EnableConfigurationProperties(AuthProperties.class)
  static class AuthPropertiesConfig {}

  @Configuration(proxyBeanMethods = false)
  @EnableConfigurationProperties(NewsfeedProperties.class)
  static class NewsfeedPropertiesConfig {}

  @Configuration(proxyBeanMethods = false)
  @EnableConfigurationProperties(CloudinaryProperties.class)
  @Import(CloudinaryConfig.class)
  static class CloudinaryIntegrationConfig {}

  @Configuration(proxyBeanMethods = false)
  @EnableConfigurationProperties({OAuth2ClientProperties.class, MailProperties.class})
  @Import(ExternalServiceConfigurationValidator.class)
  static class ExternalServicesConfig {}
}
