package com.veyru.adapter.in.dto.request.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ResetPasswordRequest(
    @NotBlank(message = "Token must not be blank") String token,
    @NotBlank(message = "New password must not be blank")
        @Size(min = 6, message = "Password must be at least 6 characters")
        String newPassword,
    @NotBlank(message = "Confirm password must not be blank") String confirmPassword) {


}
