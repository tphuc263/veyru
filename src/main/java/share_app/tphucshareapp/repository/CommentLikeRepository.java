package share_app.tphucshareapp.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import share_app.tphucshareapp.model.CommentLike;

import java.util.List;
import java.util.Optional;

public interface CommentLikeRepository extends MongoRepository<CommentLike, String> {
    boolean existsByCommentIdAndUserId(String commentId, String userId);
    
    Optional<CommentLike> findByCommentIdAndUserId(String commentId, String userId);
    
    List<CommentLike> findByCommentIdOrderByCreatedAtDesc(String commentId);
    
    long countByCommentId(String commentId);
    
    void deleteAllByCommentId(String commentId);
}
