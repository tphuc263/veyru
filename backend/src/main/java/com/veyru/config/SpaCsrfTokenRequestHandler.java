package com.veyru.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.function.Supplier;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.security.web.csrf.CsrfTokenRequestHandler;

final class SpaCsrfTokenRequestHandler implements CsrfTokenRequestHandler {
  private final CsrfTokenRequestHandler plain = new CsrfTokenRequestAttributeHandler();

  @Override
  public void handle(
      HttpServletRequest request,
      HttpServletResponse response,
      Supplier<CsrfToken> deferredCsrfToken) {
    plain.handle(request, response, deferredCsrfToken);
    deferredCsrfToken.get();
  }

  @Override
  public String resolveCsrfTokenValue(HttpServletRequest request, CsrfToken csrfToken) {
    return plain.resolveCsrfTokenValue(request, csrfToken);
  }
}
