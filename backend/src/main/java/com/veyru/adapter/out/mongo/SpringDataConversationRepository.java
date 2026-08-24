package com.veyru.adapter.out.mongo;

import java.util.List;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface SpringDataConversationRepository
    extends MongoRepository<ConversationDocument, String> {
  List<ConversationDocument> findByParticipantIdsContaining(String userId, Sort sort);
}
