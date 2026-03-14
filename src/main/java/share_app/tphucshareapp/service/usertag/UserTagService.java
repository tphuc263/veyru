package share_app.tphucshareapp.service.usertag;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;
import share_app.tphucshareapp.dto.request.usertag.CreateUserTagRequest;
import share_app.tphucshareapp.dto.response.usertag.UserTagResponse;
import share_app.tphucshareapp.model.Photo;
import share_app.tphucshareapp.model.User;
import share_app.tphucshareapp.repository.PhotoRepository;
import share_app.tphucshareapp.repository.UserRepository;
import share_app.tphucshareapp.service.notification.INotificationService;
import share_app.tphucshareapp.service.user.UserAvatarCacheService;
import share_app.tphucshareapp.service.user.UserService;

import java.time.Instant;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserTagService implements IUserTagService {

    private final PhotoRepository photoRepository;
    private final UserRepository userRepository;
    private final UserService userService;
    private final INotificationService notificationService;
    private final UserAvatarCacheService userAvatarCacheService;
    private final MongoTemplate mongoTemplate;

    @Override
    public UserTagResponse tagUserInPhoto(String photoId, CreateUserTagRequest request) {
        Photo photo = photoRepository.findById(photoId)
                .orElseThrow(() -> new RuntimeException("Photo not found with ID: " + photoId));

        User taggedUser = userRepository.findById(request.getTaggedUserId())
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + request.getTaggedUserId()));

        User currentUser = userService.getCurrentUser();

        // Check if only photo owner can tag users
        if (!photo.getUser().getUserId().equals(currentUser.getId())) {
            throw new RuntimeException("Only photo owner can tag users");
        }

        // Check if user is already tagged
        if (photo.getUserTags() != null) {
            boolean alreadyTagged = photo.getUserTags().stream()
                    .anyMatch(t -> t.getTaggedUserId().equals(request.getTaggedUserId()));
            if (alreadyTagged) {
                throw new RuntimeException("User is already tagged in this photo");
            }
        }

        Photo.EmbeddedUserTag embeddedTag = new Photo.EmbeddedUserTag(
                request.getTaggedUserId(),
                currentUser.getId(),
                taggedUser.getUsername(),
                request.getPositionX(),
                request.getPositionY(),
                Instant.now()
        );

        // Push to embedded array
        Query query = new Query(Criteria.where("_id").is(photoId));
        Update update = new Update().push("userTags", embeddedTag);
        mongoTemplate.updateFirst(query, update, Photo.class);

        // Send notification to tagged user
        notificationService.sendTagInPhotoNotification(
                request.getTaggedUserId(),
                currentUser,
                photoId,
                photo.getImageUrl()
        );

        log.info("User {} tagged user {} in photo {}", currentUser.getId(), request.getTaggedUserId(), photoId);

        return convertToResponse(embeddedTag, photoId);
    }

    @Override
    public void removeUserTag(String photoId, String taggedUserId) {
        Photo photo = photoRepository.findById(photoId)
                .orElseThrow(() -> new RuntimeException("Photo not found with ID: " + photoId));

        User currentUser = userService.getCurrentUser();

        // Check if current user is the photo owner or the tagged user
        boolean isPhotoOwner = photo.getUser().getUserId().equals(currentUser.getId());
        boolean isTaggedUser = taggedUserId.equals(currentUser.getId());

        if (!isPhotoOwner && !isTaggedUser) {
            throw new RuntimeException("Only photo owner or tagged user can remove the tag");
        }

        // Pull from embedded array
        Query query = new Query(Criteria.where("_id").is(photoId));
        Update update = new Update().pull("userTags",
                new org.bson.Document("taggedUserId", taggedUserId));
        mongoTemplate.updateFirst(query, update, Photo.class);

        log.info("Removed tag of user {} from photo {} by user {}", taggedUserId, photoId, currentUser.getId());
    }

    @Override
    public List<UserTagResponse> getPhotoUserTags(String photoId) {
        Photo photo = photoRepository.findById(photoId)
                .orElseThrow(() -> new RuntimeException("Photo not found with ID: " + photoId));

        if (photo.getUserTags() == null || photo.getUserTags().isEmpty()) {
            return Collections.emptyList();
        }

        return photo.getUserTags().stream()
                .map(tag -> convertToResponse(tag, photoId))
                .toList();
    }

    @Override
    public List<UserTagResponse> getPhotosWhereUserIsTagged(String userId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));

        // Query photos where userTags array contains an element with matching taggedUserId
        Query query = new Query(Criteria.where("userTags.taggedUserId").is(userId));
        List<Photo> photos = mongoTemplate.find(query, Photo.class);

        return photos.stream()
                .flatMap(photo -> {
                    if (photo.getUserTags() == null) return java.util.stream.Stream.empty();
                    return photo.getUserTags().stream()
                            .filter(tag -> userId.equals(tag.getTaggedUserId()))
                            .map(tag -> convertToResponse(tag, photo.getId()));
                })
                .toList();
    }

    private UserTagResponse convertToResponse(Photo.EmbeddedUserTag tag, String photoId) {
        return UserTagResponse.builder()
                .photoId(photoId)
                .taggedUserId(tag.getTaggedUserId())
                .taggedByUserId(tag.getTaggedByUserId())
                .username(tag.getUsername())
                .userImageUrl(userAvatarCacheService.getAvatar(tag.getTaggedUserId()))
                .positionX(tag.getPositionX())
                .positionY(tag.getPositionY())
                .createdAt(tag.getCreatedAt())
                .build();
    }
}
