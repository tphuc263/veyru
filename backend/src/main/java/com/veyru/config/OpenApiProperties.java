package com.veyru.config;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.net.URI;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "open.api")
public record OpenApiProperties(
    @NotBlank String title,
    @NotBlank String version,
    @NotBlank String description,
    @NotNull URI serverUrl) {
  @AssertTrue(message = "OpenAPI server URL must be an absolute HTTP(S) URL")
  public boolean isServerUrlValid() {
    return serverUrl != null
        && serverUrl.isAbsolute()
        && ("http".equalsIgnoreCase(serverUrl.getScheme())
            || "https".equalsIgnoreCase(serverUrl.getScheme()));
  }
}
