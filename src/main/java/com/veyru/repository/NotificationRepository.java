package com.veyru.repository;

import com.veyru.model.Notification;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface NotificationRepository extends MongoRepository<Notification, String> {
  Page<Notification> findByRecipientIdOrderByCreatedAtDesc(String recipientId, Pageable pageable);

  List<Notification> findByRecipientIdAndReadFalseOrderByCreatedAtDesc(String recipientId);

  long countByRecipientIdAndReadFalse(String recipientId);

  void deleteAllByRecipientId(String recipientId);
}
