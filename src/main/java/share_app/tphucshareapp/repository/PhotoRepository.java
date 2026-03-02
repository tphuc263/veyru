package share_app.tphucshareapp.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import share_app.tphucshareapp.model.Photo;

import java.time.Instant;
import java.util.List;
import org.bson.types.ObjectId;

public interface PhotoRepository extends MongoRepository<Photo, String> {
    Page<Photo> findByUserUserIdOrderByCreatedAtDesc(String userId, Pageable pageable);

    Page<Photo> findAllByOrderByCreatedAtDesc(Pageable pageable);

    @Query("{ 'caption': { $regex: ?0, $options: 'i' } }")
    Page<Photo> findByTextSearch(String searchText, Pageable pageable);

    Page<Photo> findByCaptionContainingIgnoreCase(String caption, Pageable pageable);

    Page<Photo> findByTagsIn(List<String> tagNames, Pageable pageable);

    // For newsfeed - get recent photos from followed users
    List<Photo> findByUser_UserIdInAndCreatedAtAfterOrderByCreatedAtDesc(List<String> userIds, Instant createdAt);
    
    // For newsfeed - get all photos from followed users (fallback when no recent photos)
    List<Photo> findByUser_UserIdInOrderByCreatedAtDesc(List<String> userIds);
    
    // For newsfeed - get user's own photos
    List<Photo> findByUser_UserIdOrderByCreatedAtDesc(String userId);
}