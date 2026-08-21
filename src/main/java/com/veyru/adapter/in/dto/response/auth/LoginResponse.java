package com.veyru.adapter.in.dto.response.auth;

public record LoginResponse(String jwt, String id, String username, String email, String role) {}
