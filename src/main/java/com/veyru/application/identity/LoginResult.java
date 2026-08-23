package com.veyru.application.identity;

public record LoginResult(String token, String id, String username, String email, String role) {}
