package com.veyru.repository;

import com.veyru.model.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface MessageRepository extends MongoRepository<Message, String> {

  Page<Message> findByConversationIdOrderByCreatedAtDesc(String conversationId, Pageable pageable);

  long countByConversationIdAndReceiverIdAndReadFalse(String conversationId, String receiverId);

  long countByReceiverIdAndReadFalse(String receiverId);
}
