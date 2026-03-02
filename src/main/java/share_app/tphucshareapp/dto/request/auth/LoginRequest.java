package share_app.tphucshareapp.dto.request.auth;

import lombok.Data;

@Data
public class LoginRequest {
    private String identifier; // Can be email, username, or phone number
    private String password;
}
