package com.veyru.config;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotEmpty;
import java.net.URI;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "cors")
public record CorsProperties(@NotEmpty List<String> allowedOrigins) {
  @AssertTrue(message = "CORS origins must be absolute HTTP(S) origins")
  public boolean areAllowedOriginsValid() {
    if (allowedOrigins == null) return false;
    try {
      return allowedOrigins.stream()
          .map(URI::create)
          .allMatch(
              origin ->
                  origin.isAbsolute()
                      && origin.getHost() != null
                      && origin.getPath().isEmpty()
                      && origin.getQuery() == null
                      && origin.getFragment() == null
                      && ("http".equalsIgnoreCase(origin.getScheme())
                          || "https".equalsIgnoreCase(origin.getScheme())));
    } catch (IllegalArgumentException exception) {
      return false;
    }
  }
}
