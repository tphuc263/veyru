package com.veyru.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.mail.autoconfigure.MailProperties;
import org.springframework.boot.security.oauth2.client.autoconfigure.OAuth2ClientProperties;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties({
  AuthProperties.class,
  ApplicationProperties.class,
  CloudinaryProperties.class,
  CorsProperties.class,
  NewsfeedProperties.class,
  OpenApiProperties.class,
  MailProperties.class,
  OAuth2ClientProperties.class
})
public class ConfigurationPropertiesConfig {}
