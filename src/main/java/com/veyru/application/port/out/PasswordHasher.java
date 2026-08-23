package com.veyru.application.port.out;

public interface PasswordHasher {
  String hash(String password);
}
