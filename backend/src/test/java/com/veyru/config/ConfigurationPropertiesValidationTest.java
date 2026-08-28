package com.veyru.config;

import static org.assertj.core.api.Assertions.assertThat;

import com.veyru.application.port.out.ImageStorage;
import java.time.Duration;
import java.util.Arrays;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.mail.autoconfigure.MailProperties;
import org.springframework.boot.security.oauth2.client.autoconfigure.OAuth2ClientProperties;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

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
  @ValueSource(
      strings = {
        "cloudinary.cloud-name",
        "cloudinary.api-key",
        "cloudinary.api-secret"
      })
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
