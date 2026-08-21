package com.veyru.adapter.out.mongo;

import com.veyru.application.messaging.PageResult;
import com.veyru.application.port.out.MessageStore;
import com.veyru.domain.model.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;

@Component
public class MongoMessageStore implements MessageStore {
  private final SpringDataMessageRepository repository;
  private final MongoTemplate mongoTemplate;

  public MongoMessageStore(SpringDataMessageRepository repository, MongoTemplate mongoTemplate) {
    this.repository = repository;
    this.mongoTemplate = mongoTemplate;
  }

  @Override
  public Message save(Message message) {
    return repository.save(MessageDocument.from(message)).toDomain();
  }

  @Override
  public PageResult<Message> findByConversationId(String conversationId, int page, int size) {
    Page<MessageDocument> documents =
        repository.findByConversationIdOrderByCreatedAtDesc(
            conversationId, PageRequest.of(page, size));
    return new PageResult<>(
        documents.getContent().stream().map(MessageDocument::toDomain).toList(),
        documents.getNumber(),
        documents.getSize(),
        documents.getTotalElements(),
        documents.getTotalPages());
  }

  @Override
  public long countUnread(String conversationId, String receiverId) {
    return repository.countByConversationIdAndReceiverIdAndReadFalse(conversationId, receiverId);
  }

  @Override
  public long countUnread(String receiverId) {
    return repository.countByReceiverIdAndReadFalse(receiverId);
  }

  @Override
  public long markUnreadAsRead(String conversationId, String receiverId) {
    Query query =
        new Query(
            Criteria.where("conversationId")
                .is(conversationId)
                .and("receiverId")
                .is(receiverId)
                .and("read")
                .is(false));
    return mongoTemplate
        .updateMulti(query, new Update().set("read", true), MessageDocument.class)
        .getModifiedCount();
  }
}
