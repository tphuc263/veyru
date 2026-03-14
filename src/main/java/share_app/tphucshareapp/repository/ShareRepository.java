package share_app.tphucshareapp.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import share_app.tphucshareapp.model.Share;

import java.util.List;

public interface ShareRepository extends MongoRepository<Share, String> {

    long countByPhotoId(String photoId);

    boolean existsByPhotoIdAndUserId(String photoId, String userId);

    Page<Share> findByUserIdOrderByCreatedAtDesc(String userId, Pageable pageable);

    List<Share> findByPhotoIdOrderByCreatedAtDesc(String photoId);

    List<Share> findByUserIdInOrderByCreatedAtDesc(List<String> userIds);

    void deleteAllByPhotoId(String photoId);
}
