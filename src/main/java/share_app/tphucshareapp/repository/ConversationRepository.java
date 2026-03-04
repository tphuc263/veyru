package share_app.tphucshareapp.repository;

import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.repository.MongoRepository;
import share_app.tphucshareapp.model.Conversation;

import java.util.List;

public interface ConversationRepository extends MongoRepository<Conversation, String> {

    List<Conversation> findByParticipantIdsContaining(String userId, Sort sort);
}
