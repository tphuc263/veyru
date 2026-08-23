package com.veyru.application.identity;

public record RegisterUserCommand(String username, String email, String password) {}
