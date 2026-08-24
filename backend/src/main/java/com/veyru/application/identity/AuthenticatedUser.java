package com.veyru.application.identity;

public record AuthenticatedUser(String id, String username, String email, String role) {}
