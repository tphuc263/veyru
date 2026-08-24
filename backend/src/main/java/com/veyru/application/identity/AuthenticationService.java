package com.veyru.application.identity;

import com.veyru.application.common.error.UseCaseError;
import com.veyru.application.common.error.UseCaseException;
import com.veyru.application.port.out.IdentityUserStore;
import com.veyru.application.port.out.MailSender;
import com.veyru.application.port.out.PasswordHasher;
import com.veyru.domain.model.User;
import java.time.Clock;
import java.time.temporal.ChronoUnit;
import java.util.function.Supplier;

public final class AuthenticationService {
  private static final int RESET_TOKEN_EXPIRY_MINUTES = 30;

  private final IdentityUserStore users;
  private final PasswordHasher passwords;
  private final MailSender mail;
  private final Supplier<String> resetTokens;
  private final Clock clock;

  public AuthenticationService(
      IdentityUserStore users,
      PasswordHasher passwords,
      MailSender mail,
      Supplier<String> resetTokens,
      Clock clock) {
    this.users = users;
    this.passwords = passwords;
    this.mail = mail;
    this.resetTokens = resetTokens;
    this.clock = clock;
  }

  public void register(RegisterUserCommand command) {
    if (users.existsByEmail(command.email())) {
      throw new UseCaseException(UseCaseError.RESOURCE_CONFLICT);
    }
    users.save(
        User.registered(
            command.username(),
            command.email(),
            passwords.hash(command.password()),
            clock.instant()));
  }

  public void forgotPassword(String email) {
    users
        .findByEmail(email)
        .ifPresent(
            user -> {
              String token = resetTokens.get();
              User updated =
                  user.requestPasswordReset(
                      token, clock.instant().plus(RESET_TOKEN_EXPIRY_MINUTES, ChronoUnit.MINUTES));
              users.save(updated);
              mail.sendPasswordReset(updated.getEmail(), token, updated.getUsername());
            });
  }

  public void resetPassword(ResetPasswordCommand command) {
    if (!command.newPassword().equals(command.confirmPassword())) {
      throw new UseCaseException(UseCaseError.VALIDATION_FAILED);
    }
    User user =
        users
            .findByResetToken(command.token())
            .filter(account -> account.hasValidResetToken(clock.instant()))
            .orElseThrow(() -> new UseCaseException(UseCaseError.VALIDATION_FAILED));
    users.save(user.resetPassword(passwords.hash(command.newPassword())));
  }
}
