package com.veyru.adapter.in.web;

import com.veyru.adapter.in.dto.response.auth.CsrfResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import org.springframework.http.ResponseEntity;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("${api.prefix}/csrf")
@SecurityRequirements
public class CsrfController {
  @GetMapping
  public ResponseEntity<CsrfResponse> csrf(CsrfToken token) {
    return ResponseEntity.ok(new CsrfResponse(token.getToken()));
  }
}
