package com.veyru.application.port.out;

import com.veyru.application.result.notification.NotificationResult;

public interface NotificationNotifier {
  void send(String userId, NotificationResult notification);
}
