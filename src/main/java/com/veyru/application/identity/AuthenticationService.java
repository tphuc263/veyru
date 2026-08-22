package com.veyru.application.identity;

import com.veyru.application.port.out.AuthenticationGateway;
import com.veyru.application.port.out.IdentityUserStore;
import com.veyru.application.port.out.MailSender;
import com.veyru.application.port.out.PasswordHasher;
import java.time.Clock;
import java.time.temporal.ChronoUnit;
import java.util.function.Supplier;

public final class AuthenticationService {
  private static final int RESET_TOKEN_EXPIRY_MINUTES = 30;

  private final AuthenticationGateway authentication;
  private final IdentityUserStore users;
  private final PasswordHasher passwords;
  private final MailSender mail;
  private final Supplier<String> resetTokens;
  private final Clock clock;

  public AuthenticationService(
      AuthenticationGateway authentication,
      IdentityUserStore users,
      PasswordHasher passwords,
      MailSender mail,
      Supplier<String> resetTokens,
      Clock clock) {
    this.authentication = authentication;
    this.users = users;
    this.passwords = passwords;
    this.mail = mail;
    this.resetTokens = resetTokens;
    this.clock = clock;
  }

  public LoginResult login(LoginCommand command) {
    return authentication.authenticate(command.identifier(), command.password());
  }

  public void register(RegisterUserCommand command) {
    if (users.existsByEmail(command.email())) {
      throw new IdentityException(IdentityException.Reason.CONFLICT);
    }
    users.save(
        UserAccount.registered(
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
              UserAccount updated =
                  user.requestPasswordReset(
                      token, clock.instant().plus(RESET_TOKEN_EXPIRY_MINUTES, ChronoUnit.MINUTES));
              users.save(updated);
              mail.sendPasswordReset(updated.email(), token, updated.username());
            });
  }

  public void resetPassword(ResetPasswordCommand command) {
    if (!command.newPassword().equals(command.confirmPassword())) {
      throw new IdentityException(IdentityException.Reason.VALIDATION_FAILED);
    }
    UserAccount user =
        users
            .findByResetToken(command.token())
            .filter(account -> account.hasValidResetToken(clock.instant()))
            .orElseThrow(
                () -> new IdentityException(IdentityException.Reason.VALIDATION_FAILED));
    users.save(user.resetPassword(passwords.hash(command.newPassword())));
  }

  public boolean validateResetToken(String token) {
    return users
        .findByResetToken(token)
        .map(account -> account.hasValidResetToken(clock.instant()))
        .orElse(false);
  }
}
