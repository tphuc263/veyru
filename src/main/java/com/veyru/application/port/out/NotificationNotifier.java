package com.veyru.application.port.out;

public interface NotificationNotifier {
  void send(String userId, Object notification);
}
