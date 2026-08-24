package com.veyru.application.identity;

public record SessionTokens(
    AuthenticatedUser user, String accessToken, String refreshToken, long accessMaxAgeSeconds) {}
