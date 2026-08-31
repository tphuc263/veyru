package com.veyru.adapter.out.mail;

import com.veyru.application.common.error.UseCaseError;
import com.veyru.application.common.error.UseCaseException;
import com.veyru.application.port.out.MailSender;
import com.veyru.config.ApplicationProperties;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.boot.mail.autoconfigure.MailProperties;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class SmtpMailSender implements MailSender {
  private final JavaMailSender mailSender;
  private final String fromEmail;
  private final String frontendUrl;

  public void sendPasswordResetEmail(String toEmail, String token, String username) {
    try {
      MimeMessage message = mailSender.createMimeMessage();
      MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
      helper.setFrom(fromEmail);
      helper.setTo(toEmail);
      helper.setSubject("Veyru - Password Reset Request");
      String resetLink = frontendUrl + "/reset-password?token=" + token;
      String htmlContent = buildPasswordResetEmail(username, resetLink);
      helper.setText(htmlContent, true);
      mailSender.send(message);
    } catch (MessagingException | MailException e) {
      throw new UseCaseException(UseCaseError.EXTERNAL_SERVICE_FAILURE, e);
    }
  }

  @Override
  public void sendPasswordReset(String email, String token, String username) {
    sendPasswordResetEmail(email, token, username);
  }

  private String buildPasswordResetEmail(String username, String resetLink) {
    return """
      <!DOCTYPE html>
      <html>
      <head>
          <meta charset=\"UTF-8\">
          <style>
              body { font-family: -apple-system, BlinkMacSystemFont, \'Segoe UI\', Roboto, sans-serif; margin: 0; padding: 0; background-color: #fafafa; }
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
          <div class=\"container\">
              <div class=\"header\">
                  <h1>Veyru</h1>
              </div>
              <div class=\"body\">
                  <h2>Hi %s,</h2>
                  <p>We received a request to reset your password. Click the button below to create a new password:</p>
                  <a href=\"%s\" class=\"reset-btn\">Reset Password</a>
                  <p>This link will expire in <strong>30 minutes</strong>.</p>
                  <p>If you didn\'t request a password reset, you can safely ignore this email. Your password will remain unchanged.</p>
              </div>
              <div class=\"footer\">
                  <p>&copy; 2026 Veyru. All rights reserved.</p>
              </div>
          </div>
      </body>
      </html>
      """
        .formatted(username, resetLink);
  }

  public SmtpMailSender(
      JavaMailSender mailSender,
      MailProperties mailProperties,
      ApplicationProperties applicationProperties) {
    this.mailSender = mailSender;
    this.fromEmail = mailProperties.getUsername();
    this.frontendUrl = applicationProperties.frontend().url().toString();
  }
}
