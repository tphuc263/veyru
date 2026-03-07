package share_app.tphucshareapp.service.usertag;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import share_app.tphucshareapp.dto.request.usertag.CreateUserTagRequest;
import share_app.tphucshareapp.dto.response.usertag.UserTagResponse;
import share_app.tphucshareapp.model.Photo;
import share_app.tphucshareapp.model.User;
import share_app.tphucshareapp.model.UserTag;
import share_app.tphucshareapp.repository.PhotoRepository;
import share_app.tphucshareapp.repository.UserRepository;
import share_app.tphucshareapp.repository.UserTagRepository;
import share_app.tphucshareapp.service.notification.INotificationService;
import share_app.tphucshareapp.service.user.UserService;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserTagService implements IUserTagService {
    
    private final UserTagRepository userTagRepository;
    private final PhotoRepository photoRepository;
    private final UserRepository userRepository;
    private final UserService userService;
    private final INotificationService notificationService;

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
        if (userTagRepository.existsByPhotoIdAndTaggedUserId(photoId, request.getTaggedUserId())) {
            throw new RuntimeException("User is already tagged in this photo");
        }
        
        UserTag userTag = new UserTag();
        userTag.setPhotoId(photoId);
        userTag.setTaggedUserId(request.getTaggedUserId());
        userTag.setTaggedByUserId(currentUser.getId());
        userTag.setPositionX(request.getPositionX());
        userTag.setPositionY(request.getPositionY());
        userTag.setCreatedAt(Instant.now());
        userTag.setTaggedUser(new UserTag.EmbeddedUser(taggedUser.getUsername(), taggedUser.getImageUrl()));
        
        UserTag savedTag = userTagRepository.save(userTag);
        
        // Send notification to tagged user
        notificationService.sendTagInPhotoNotification(
                request.getTaggedUserId(),
                currentUser,
                photoId,
                photo.getImageUrl()
        );
        
        log.info("User {} tagged user {} in photo {}", currentUser.getId(), request.getTaggedUserId(), photoId);
        
        return convertToResponse(savedTag);
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
        
        userTagRepository.deleteByPhotoIdAndTaggedUserId(photoId, taggedUserId);
        
        log.info("Removed tag of user {} from photo {} by user {}", taggedUserId, photoId, currentUser.getId());
    }

    @Override
    public List<UserTagResponse> getPhotoUserTags(String photoId) {
        photoRepository.findById(photoId)
                .orElseThrow(() -> new RuntimeException("Photo not found with ID: " + photoId));
        
        return userTagRepository.findByPhotoId(photoId).stream()
                .map(this::convertToResponse)
                .toList();
    }

    @Override
    public List<UserTagResponse> getPhotosWhereUserIsTagged(String userId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));
        
        return userTagRepository.findByTaggedUserId(userId).stream()
                .map(this::convertToResponse)
                .toList();
    }
    
    private UserTagResponse convertToResponse(UserTag userTag) {
        return UserTagResponse.builder()
                .id(userTag.getId())
                .photoId(userTag.getPhotoId())
                .taggedUserId(userTag.getTaggedUserId())
                .taggedByUserId(userTag.getTaggedByUserId())
                .username(userTag.getTaggedUser() != null ? userTag.getTaggedUser().getUsername() : null)
                .userImageUrl(userTag.getTaggedUser() != null ? userTag.getTaggedUser().getUserImageUrl() : null)
                .positionX(userTag.getPositionX())
                .positionY(userTag.getPositionY())
                .createdAt(userTag.getCreatedAt())
                .build();
    }
}
