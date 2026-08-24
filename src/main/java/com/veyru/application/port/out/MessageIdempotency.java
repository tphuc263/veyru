package com.veyru.application.port.out;

public interface MessageIdempotency {
  boolean claim(String senderId, String clientMessageId);
}
