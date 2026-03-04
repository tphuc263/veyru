package share_app.tphucshareapp.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import share_app.tphucshareapp.model.Message;

public interface MessageRepository extends MongoRepository<Message, String> {

    Page<Message> findByConversationIdOrderByCreatedAtDesc(String conversationId, Pageable pageable);

    long countByConversationIdAndReceiverIdAndReadFalse(String conversationId, String receiverId);

    long countByReceiverIdAndReadFalse(String receiverId);
}
