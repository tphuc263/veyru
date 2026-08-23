package com.veyru.application.port.out;

import com.veyru.domain.model.Notification;
import java.util.List;
import java.util.Optional;

public interface NotificationStore {
  Notification save(Notification notification);

  void saveAll(List<Notification> notifications);

  Optional<Notification> findById(String id);

  List<Notification> findByRecipient(String recipientId, int page, int size);

  List<Notification> findUnread(String recipientId);

  long countUnread(String recipientId);
}
