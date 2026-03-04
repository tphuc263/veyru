package share_app.tphucshareapp.service.share;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;
import share_app.tphucshareapp.dto.response.photo.PhotoResponse;
import share_app.tphucshareapp.dto.response.share.ShareResponse;
import share_app.tphucshareapp.model.Photo;
import share_app.tphucshareapp.model.Share;
import share_app.tphucshareapp.model.User;
import share_app.tphucshareapp.repository.PhotoRepository;
import share_app.tphucshareapp.repository.ShareRepository;
import share_app.tphucshareapp.repository.UserRepository;
import share_app.tphucshareapp.service.photo.PhotoConversionService;
import share_app.tphucshareapp.service.user.UserService;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ShareService implements IShareService {

    private final ShareRepository shareRepository;
    private final PhotoRepository photoRepository;
    private final UserRepository userRepository;
    private final UserService userService;
    private final MongoTemplate mongoTemplate;
    private final PhotoConversionService photoConversionService;

    @Override
    public PhotoResponse sharePhoto(String photoId, String caption) {
        User currentUser = userService.getCurrentUser();
        Photo photo = photoRepository.findById(photoId)
                .orElseThrow(() -> new RuntimeException("Photo not found with ID: " + photoId));

        // Create share record
        Share share = new Share();
        share.setPhotoId(photoId);
        share.setUserId(currentUser.getId());
        share.setCaption(caption);
        share.setCreatedAt(Instant.now());
        shareRepository.save(share);

        // Increment share count on photo
        Query query = new Query(Criteria.where("_id").is(photoId));
        Update update = new Update().inc("shareCount", 1);
        mongoTemplate.updateFirst(query, update, Photo.class);
        photo.setShareCount(photo.getShareCount() + 1);

        log.info("User {} shared photo {} to their profile", currentUser.getId(), photoId);

        return photoConversionService.convertToPhotoResponse(photo, currentUser);
    }

    @Override
    public List<ShareResponse> getPhotoShares(String photoId) {
        List<Share> shares = shareRepository.findByPhotoIdOrderByCreatedAtDesc(photoId);

        List<String> userIds = shares.stream()
                .map(Share::getUserId)
                .distinct()
                .toList();

        Map<String, User> userMap = userRepository.findAllById(userIds).stream()
                .collect(Collectors.toMap(User::getId, u -> u));

        return shares.stream().map(share -> {
            ShareResponse response = new ShareResponse();
            response.setId(share.getId());
            response.setPhotoId(share.getPhotoId());
            response.setUserId(share.getUserId());
            response.setCaption(share.getCaption());
            response.setCreatedAt(share.getCreatedAt());

            User user = userMap.get(share.getUserId());
            if (user != null) {
                response.setUsername(user.getUsername());
                response.setUserImageUrl(user.getImageUrl());
            }
            return response;
        }).toList();
    }

    @Override
    public long getShareCount(String photoId) {
        return shareRepository.countByPhotoId(photoId);
    }

    @Override
    public boolean hasShared(String photoId) {
        User currentUser = userService.getCurrentUser();
        return shareRepository.existsByPhotoIdAndUserId(photoId, currentUser.getId());
    }
}
