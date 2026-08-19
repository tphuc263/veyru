package com.veyru.service.auth;

import com.veyru.dto.request.auth.ForgotPasswordRequest;
import com.veyru.dto.request.auth.LoginRequest;
import com.veyru.dto.request.auth.RegisterRequest;
import com.veyru.dto.request.auth.ResetPasswordRequest;
import com.veyru.dto.response.auth.LoginResponse;

public interface IAuthService {
    LoginResponse login(LoginRequest request);

    void register(RegisterRequest request);

    void forgotPassword(ForgotPasswordRequest request);

    void resetPassword(ResetPasswordRequest request);

    boolean validateResetToken(String token);
}
