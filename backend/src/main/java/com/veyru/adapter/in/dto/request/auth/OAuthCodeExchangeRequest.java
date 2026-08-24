package com.veyru.adapter.in.dto.request.auth;

import jakarta.validation.constraints.NotBlank;

public record OAuthCodeExchangeRequest(@NotBlank String code) {}
