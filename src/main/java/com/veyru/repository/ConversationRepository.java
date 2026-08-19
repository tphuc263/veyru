package com.veyru.repository;

import com.veyru.model.Conversation;
import java.util.List;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ConversationRepository extends MongoRepository<Conversation, String> {

  List<Conversation> findByParticipantIdsContaining(String userId, Sort sort);
}
