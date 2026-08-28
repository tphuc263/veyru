package com.veyru.config;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.mail.autoconfigure.MailProperties;
import org.springframework.boot.security.oauth2.client.autoconfigure.OAuth2ClientProperties;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class ExternalServiceConfigurationValidator implements InitializingBean {
  private static final String GOOGLE_REGISTRATION = "google";

  private final OAuth2ClientProperties oauth2;
  private final MailProperties mail;

  public ExternalServiceConfigurationValidator(
      OAuth2ClientProperties oauth2, MailProperties mail) {
    this.oauth2 = oauth2;
    this.mail = mail;
  }

  @Override
  public void afterPropertiesSet() {
    validateGoogleOAuth();
    validateMail();
  }

  private void validateGoogleOAuth() {
    OAuth2ClientProperties.Registration google =
        oauth2.getRegistration().get(GOOGLE_REGISTRATION);
    if (google == null) {
      throw missing("spring.security.oauth2.client.registration.google");
    }
    requireText(
        google.getClientId(),
        "spring.security.oauth2.client.registration.google.client-id");
    requireText(
        google.getClientSecret(),
        "spring.security.oauth2.client.registration.google.client-secret");
  }

  private void validateMail() {
    requireText(mail.getHost(), "spring.mail.host");
    if (mail.getPort() == null || mail.getPort() <= 0) {
      throw new IllegalStateException("Required property spring.mail.port must be positive");
    }
    requireText(mail.getUsername(), "spring.mail.username");
    requireText(mail.getPassword(), "spring.mail.password");
  }

  private static void requireText(String value, String propertyName) {
    if (!StringUtils.hasText(value)) {
      throw missing(propertyName);
    }
  }

  private static IllegalStateException missing(String propertyName) {
    return new IllegalStateException("Required property " + propertyName + " is missing");
  }
}
