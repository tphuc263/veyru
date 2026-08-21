package com.veyru.adapter.out.mongo;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface SpringDataMessageRepository extends MongoRepository<MessageDocument, String> {
  Page<MessageDocument> findByConversationIdOrderByCreatedAtDesc(
      String conversationId, Pageable pageable);

  long countByConversationIdAndReceiverIdAndReadFalse(String conversationId, String receiverId);

  long countByReceiverIdAndReadFalse(String receiverId);
}
