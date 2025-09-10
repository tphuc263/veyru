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

    // Text search in captions - simplified approach
    @Query("{ 'caption': { $regex: ?0, $options: 'i' } }")
    Page<Photo> findByTextSearch(String searchText, Pageable pageable);

    // Caption search (partial match) - this works better for simple cases
    Page<Photo> findByCaptionContainingIgnoreCase(String caption, Pageable pageable);

    Page<Photo> findByTagsIn(List<String> tagNames, Pageable pageable);

//    Page<Photo> findByUserUserId(String userId, Pageable pageable);

    List<Photo> findByUser_UserIdInAndCreatedAtAfterOrderByCreatedAtDesc(List<String> userIds, Instant createdAt);
}