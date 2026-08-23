package com.veyru.application.identity;

public record ResetPasswordCommand(String token, String newPassword, String confirmPassword) {}
