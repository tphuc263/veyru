package com.veyru.application.port.out;

public interface MailSender {
  void sendPasswordReset(String email, String token, String username);
}
