package com.veyru.config;

import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import java.net.URI;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "app")
public record ApplicationProperties(
    @NotNull @Valid Frontend frontend, @NotNull @Valid OAuth2 oauth2) {
  public record Frontend(@NotNull URI url) {
    @AssertTrue(message = "Frontend URL must be an absolute HTTP(S) URL")
    public boolean isUrlValid() {
      return isHttpUrl(url);
    }
  }

  public record OAuth2(@NotNull URI redirectUri, @NotNull URI failureRedirectUri) {
    @AssertTrue(message = "OAuth redirect URLs must be absolute HTTP(S) URLs")
    public boolean areRedirectUrisValid() {
      return isHttpUrl(redirectUri) && isHttpUrl(failureRedirectUri);
    }
  }

  private static boolean isHttpUrl(URI uri) {
    return uri != null
        && uri.isAbsolute()
        && ("http".equalsIgnoreCase(uri.getScheme()) || "https".equalsIgnoreCase(uri.getScheme()));
  }
}
