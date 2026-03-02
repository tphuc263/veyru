package share_app.tphucshareapp.service.favorite;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import share_app.tphucshareapp.dto.response.photo.PhotoResponse;
import share_app.tphucshareapp.model.Favorite;
import share_app.tphucshareapp.model.Photo;
import share_app.tphucshareapp.model.User;
import share_app.tphucshareapp.repository.FavoriteRepository;
import share_app.tphucshareapp.repository.PhotoRepository;
import share_app.tphucshareapp.service.photo.PhotoConversionService;
import share_app.tphucshareapp.service.user.UserService;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class FavoriteService implements IFavoriteService {

    private final FavoriteRepository favoriteRepository;
    private final PhotoRepository photoRepository;
    private final UserService userService;
    private final PhotoConversionService photoConversionService;

    @Override
    public PhotoResponse toggleFavorite(String photoId) {
        User currentUser = userService.getCurrentUser();
        Photo photo = photoRepository.findById(photoId)
                .orElseThrow(() -> new RuntimeException("Photo not found with ID: " + photoId));

        Optional<Favorite> existingFavorite = favoriteRepository.findByUserIdAndPhotoId(currentUser.getId(), photoId);

        if (existingFavorite.isPresent()) {
            // Unsave
            favoriteRepository.delete(existingFavorite.get());
            log.info("User {} unsaved photo {}", currentUser.getId(), photoId);
        } else {
            // Save
            Favorite favorite = new Favorite();
            favorite.setUserId(currentUser.getId());
            favorite.setPhotoId(photoId);
            favorite.setCreatedAt(Instant.now());
            favoriteRepository.save(favorite);
            log.info("User {} saved photo {}", currentUser.getId(), photoId);
        }

        return photoConversionService.convertToPhotoResponse(photo, currentUser);
    }

    @Override
    public List<PhotoResponse> getFavorites(int page, int size) {
        User currentUser = userService.getCurrentUser();
        Pageable pageable = PageRequest.of(page, size);
        Page<Favorite> favorites = favoriteRepository.findByUserIdOrderByCreatedAtDesc(currentUser.getId(), pageable);

        return favorites.getContent().stream()
                .map(favorite -> {
                    Optional<Photo> photoOpt = photoRepository.findById(favorite.getPhotoId());
                    return photoOpt.map(photo -> photoConversionService.convertToPhotoResponse(photo, currentUser));
                })
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    @Override
    public boolean isFavorited(String photoId) {
        User currentUser = userService.getCurrentUser();
        return favoriteRepository.existsByUserIdAndPhotoId(currentUser.getId(), photoId);
    }
}
