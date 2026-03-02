package share_app.tphucshareapp.security.oauth2;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;
import share_app.tphucshareapp.security.jwt.JwtUtils;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtUtils jwtUtils;

    @Value("${app.oauth2.redirectUri}")
    private String defaultRedirectUri;

    @Value("${app.oauth2.failureRedirectUri}")
    private String defaultFailureRedirectUri;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {

        if (!(authentication.getPrincipal() instanceof CustomOAuth2User customUser)) {
            log.error("Principal type invalid: {}", authentication.getPrincipal().getClass().getName());
            sendErrorRedirect(response, "invalid_principal_type");
            return;
        }

        String email = customUser.getEmail();
        if (email == null || email.isEmpty()) {
            sendErrorRedirect(response, "email_not_found");
            return;
        }

        String accessToken = jwtUtils.generateToken(email, customUser.getUser().getId(), customUser.getUser().getRole().name());
        String redirectUrl = UriComponentsBuilder.fromUriString(defaultRedirectUri)
                .queryParam("token", accessToken)
                .build().toUriString();

        log.info("OAuth2 login success for {}, redirect to {}", email, redirectUrl);
        getRedirectStrategy().sendRedirect(request, response, redirectUrl);
    }

    private void sendErrorRedirect(HttpServletResponse response, String code) throws IOException {
        String errorMsg = URLEncoder.encode("OAuth2 error: " + code, StandardCharsets.UTF_8);
        String redirectUrl = UriComponentsBuilder.fromUriString(defaultFailureRedirectUri)
                .queryParam("error", errorMsg)
                .build().toUriString();
        response.sendRedirect(redirectUrl);
    }
}
