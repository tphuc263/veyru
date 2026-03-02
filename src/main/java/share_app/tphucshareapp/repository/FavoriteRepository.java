package share_app.tphucshareapp.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import share_app.tphucshareapp.model.Favorite;

import java.util.List;
import java.util.Optional;

public interface FavoriteRepository extends MongoRepository<Favorite, String> {

    Optional<Favorite> findByUserIdAndPhotoId(String userId, String photoId);

    boolean existsByUserIdAndPhotoId(String userId, String photoId);

    Page<Favorite> findByUserIdOrderByCreatedAtDesc(String userId, Pageable pageable);

    List<Favorite> findByUserId(String userId);

    void deleteByUserIdAndPhotoId(String userId, String photoId);

    void deleteAllByPhotoId(String photoId);
}
