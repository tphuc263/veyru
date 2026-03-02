package share_app.tphucshareapp.service.auth;

import share_app.tphucshareapp.dto.request.auth.ForgotPasswordRequest;
import share_app.tphucshareapp.dto.request.auth.LoginRequest;
import share_app.tphucshareapp.dto.request.auth.RegisterRequest;
import share_app.tphucshareapp.dto.request.auth.ResetPasswordRequest;
import share_app.tphucshareapp.dto.response.auth.LoginResponse;

public interface IAuthService {
    LoginResponse login(LoginRequest request);

    void register(RegisterRequest request);

    void forgotPassword(ForgotPasswordRequest request);

    void resetPassword(ResetPasswordRequest request);

    boolean validateResetToken(String token);
}
