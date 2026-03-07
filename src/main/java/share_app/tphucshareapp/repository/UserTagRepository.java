package share_app.tphucshareapp.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import share_app.tphucshareapp.model.UserTag;

import java.util.List;
import java.util.Optional;

public interface UserTagRepository extends MongoRepository<UserTag, String> {
    List<UserTag> findByPhotoId(String photoId);
    
    List<UserTag> findByTaggedUserId(String taggedUserId);
    
    Optional<UserTag> findByPhotoIdAndTaggedUserId(String photoId, String taggedUserId);
    
    boolean existsByPhotoIdAndTaggedUserId(String photoId, String taggedUserId);
    
    void deleteAllByPhotoId(String photoId);
    
    void deleteByPhotoIdAndTaggedUserId(String photoId, String taggedUserId);
    
    long countByPhotoId(String photoId);
}
