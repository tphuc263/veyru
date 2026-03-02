package share_app.tphucshareapp.service.auth;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import share_app.tphucshareapp.dto.request.auth.ForgotPasswordRequest;
import share_app.tphucshareapp.dto.request.auth.LoginRequest;
import share_app.tphucshareapp.dto.request.auth.RegisterRequest;
import share_app.tphucshareapp.dto.request.auth.ResetPasswordRequest;
import share_app.tphucshareapp.dto.response.auth.LoginResponse;
import share_app.tphucshareapp.enums.UserRole;
import share_app.tphucshareapp.exceptions.DuplicateResourceException;
import share_app.tphucshareapp.exceptions.ResourceNotFoundException;
import share_app.tphucshareapp.model.User;
import share_app.tphucshareapp.repository.UserRepository;
import share_app.tphucshareapp.security.jwt.JwtUtils;
import share_app.tphucshareapp.security.userdetails.AppUserDetails;
import share_app.tphucshareapp.service.email.EmailService;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService implements IAuthService {
    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    private static final int RESET_TOKEN_EXPIRY_MINUTES = 30;

    @Override
    public LoginResponse login(LoginRequest request) {
        log.info("Login attempt for identifier: {}", request.getIdentifier());
        
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getIdentifier(), request.getPassword()));
        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateAccessToken(authentication);

        AppUserDetails userDetails = (AppUserDetails) authentication.getPrincipal();
        
        log.info("Login successful for user: {}", userDetails.getUsername());

        return new LoginResponse(
                jwt,
                userDetails.getId(),
                userDetails.getUsername(),
                userDetails.getEmail(),
                userDetails.getAuthorities().stream()
                        .map(GrantedAuthority::getAuthority)
                        .findFirst()
                        .orElse("")
        );
    }

    @Override
    public void register(RegisterRequest request) {
        log.info("Registration attempt for email: {}", request.getEmail());
        
        if (userRepository.existsByEmail(request.getEmail())) {
            log.warn("Registration failed: Email already exists - {}", request.getEmail());
            throw new DuplicateResourceException("Email already exists");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(UserRole.ROLE_USER);
        user.setCreatedAt(Instant.now());

        userRepository.save(user);
        log.info("User registered successfully: {}", request.getEmail());
    }

    @Override
    public void forgotPassword(ForgotPasswordRequest request) {
        log.info("Password reset requested for email: {}", request.getEmail());

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> {
                    log.warn("Password reset failed: Email not found - {}", request.getEmail());
                    return new ResourceNotFoundException("No account found with this email address");
                });

        // Generate new token and store on user
        String token = UUID.randomUUID().toString();

        user.setResetToken(token);
        user.setResetTokenExpiry(Instant.now().plus(RESET_TOKEN_EXPIRY_MINUTES, ChronoUnit.MINUTES));
        userRepository.save(user);

        // Send email asynchronously
        emailService.sendPasswordResetEmail(user.getEmail(), token, user.getUsername());

        log.info("Password reset token generated for user: {}", user.getEmail());
    }

    @Override
    public void resetPassword(ResetPasswordRequest request) {
        log.info("Password reset attempt with token");

        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new IllegalArgumentException("Passwords do not match");
        }

        User user = userRepository.findByResetToken(request.getToken())
                .orElseThrow(() -> new ResourceNotFoundException("Invalid or expired reset token"));

        if (!user.isResetTokenValid()) {
            log.warn("Password reset failed: Token expired");
            throw new IllegalArgumentException("Reset token has expired. Please request a new one.");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        // Clear the reset token
        user.setResetToken(null);
        user.setResetTokenExpiry(null);
        userRepository.save(user);

        log.info("Password reset successful for user: {}", user.getEmail());
    }

    @Override
    public boolean validateResetToken(String token) {
        return userRepository.findByResetToken(token)
                .map(User::isResetTokenValid)
                .orElse(false);
    }
}
