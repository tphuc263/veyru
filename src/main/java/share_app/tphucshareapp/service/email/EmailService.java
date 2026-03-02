package share_app.tphucshareapp.service.email;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username:noreply@shareapp.com}")
    private String fromEmail;

    @Value("${app.frontend.url:http://localhost:5173}")
    private String frontendUrl;

    @Async
    public void sendPasswordResetEmail(String toEmail, String token, String username) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("Share App - Password Reset Request");

            String resetLink = frontendUrl + "/reset-password?token=" + token;
            String htmlContent = buildPasswordResetEmail(username, resetLink);

            helper.setText(htmlContent, true);
            mailSender.send(message);

            log.info("Password reset email sent to: {}", toEmail);
        } catch (MessagingException e) {
            log.error("Failed to send password reset email to: {}", toEmail, e);
            throw new RuntimeException("Failed to send password reset email", e);
        }
    }

    private String buildPasswordResetEmail(String username, String resetLink) {
        return """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                    <style>
                        body { font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif; margin: 0; padding: 0; background-color: #fafafa; }
                        .container { max-width: 520px; margin: 40px auto; background: #ffffff; border-radius: 8px; border: 1px solid #dbdbdb; overflow: hidden; }
                        .header { background: linear-gradient(45deg, #f09433, #e6683c, #dc2743, #cc2366, #bc1888); padding: 32px; text-align: center; }
                        .header h1 { color: #ffffff; font-size: 28px; margin: 0; font-weight: 700; }
                        .body { padding: 32px; }
                        .body h2 { color: #262626; font-size: 20px; margin: 0 0 16px 0; }
                        .body p { color: #8e8e8e; font-size: 14px; line-height: 1.6; margin: 0 0 16px 0; }
                        .reset-btn { display: inline-block; background-color: #0095f6; color: #ffffff !important; text-decoration: none; padding: 12px 32px; border-radius: 8px; font-size: 14px; font-weight: 600; margin: 16px 0; }
                        .footer { padding: 24px 32px; border-top: 1px solid #dbdbdb; text-align: center; }
                        .footer p { color: #8e8e8e; font-size: 12px; margin: 0; }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <div class="header">
                            <h1>Share App</h1>
                        </div>
                        <div class="body">
                            <h2>Hi %s,</h2>
                            <p>We received a request to reset your password. Click the button below to create a new password:</p>
                            <a href="%s" class="reset-btn">Reset Password</a>
                            <p>This link will expire in <strong>30 minutes</strong>.</p>
                            <p>If you didn't request a password reset, you can safely ignore this email. Your password will remain unchanged.</p>
                        </div>
                        <div class="footer">
                            <p>&copy; 2026 Share App. All rights reserved.</p>
                        </div>
                    </div>
                </body>
                </html>
                """.formatted(username, resetLink);
    }
}
