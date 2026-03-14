package share_app.tphucshareapp.service.post;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import share_app.tphucshareapp.dto.response.post.UnifiedPostResponse;
import share_app.tphucshareapp.model.Photo;
import share_app.tphucshareapp.model.Share;
import share_app.tphucshareapp.model.User;
import share_app.tphucshareapp.repository.PhotoRepository;
import share_app.tphucshareapp.repository.ShareRepository;
import share_app.tphucshareapp.repository.UserRepository;
import share_app.tphucshareapp.service.user.UserAvatarCacheService;
import share_app.tphucshareapp.service.user.UserService;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UnifiedPostService {

    private final PhotoRepository photoRepository;
    private final ShareRepository shareRepository;
    private final UserRepository userRepository;
    private final UserService userService;
    private final UserAvatarCacheService userAvatarCacheService;

    /**
     * Get unified posts (photos + shares) for a user's profile
     * Sorted by createdAt descending (newest first)
     */
    public Page<UnifiedPostResponse> getUserPosts(String userId, int page, int size) {
        log.info("Fetching unified posts for user: {}", userId);

        Pageable pageable = PageRequest.of(page, size);

        // Fetch photos
        Page<Photo> photosPage = photoRepository.findByUserUserIdOrderByCreatedAtDesc(userId, pageable);
        List<Photo> photos = photosPage.getContent();

        // Fetch shares
        Page<Share> sharesPage = shareRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
        List<Share> shares = sharesPage.getContent();

        // Convert to unified posts
        List<UnifiedPostResponse> allPosts = new ArrayList<>();

        // Add photos
        User currentUser = null;
        try {
            currentUser = userService.getCurrentUser();
        } catch (Exception e) {
            // Not logged in, that's okay
        }

        for (Photo photo : photos) {
            UnifiedPostResponse post = convertPhotoToUnifiedPost(photo, currentUser);
            allPosts.add(post);
        }

        // Add shares
        Map<String, Photo> photoMap = photoRepository.findAllById(
                shares.stream().map(Share::getPhotoId).distinct().toList()
        ).stream().collect(Collectors.toMap(Photo::getId, p -> p));

        // Get original users
        List<String> originalUserIds = photoMap.values().stream()
                .map(p -> p.getUser() != null ? p.getUser().getUserId() : null)
                .filter(id -> id != null)
                .distinct()
                .toList();

        Map<String, User> userMap = userRepository.findByIdIn(originalUserIds).stream()
                .collect(Collectors.toMap(User::getId, u -> u));

        // Get sharer info
        User sharerUser = userRepository.findById(userId).orElse(null);

        for (Share share : shares) {
            Photo originalPhoto = photoMap.get(share.getPhotoId());
            if (originalPhoto != null) {
                UnifiedPostResponse post = convertShareToUnifiedPost(share, originalPhoto, sharerUser, userMap);
                allPosts.add(post);
            }
        }

        // Sort by createdAt descending
        allPosts.sort(Comparator.comparing(UnifiedPostResponse::getCreatedAt).reversed());

        // Paginate
        int start = (int) pageable.getOffset();
        int end = Math.min(start + size, allPosts.size());

        if (start >= allPosts.size()) {
            return new PageImpl<>(List.of(), pageable, allPosts.size());
        }

        List<UnifiedPostResponse> pagePosts = allPosts.subList(start, end);
        return new PageImpl<>(pagePosts, pageable, allPosts.size());
    }

    private UnifiedPostResponse convertPhotoToUnifiedPost(Photo photo, User currentUser) {
        UnifiedPostResponse post = new UnifiedPostResponse();
        post.setId(photo.getId());
        post.setType(UnifiedPostResponse.PostType.PHOTO);
        post.setCreatedAt(photo.getCreatedAt());

        if (photo.getUser() != null) {
            post.setUserId(photo.getUser().getUserId());
            post.setUsername(photo.getUser().getUsername());
            post.setUserImageUrl(userAvatarCacheService.getAvatar(photo.getUser().getUserId()));
        }

        post.setImageUrl(photo.getImageUrl());
        post.setCaption(photo.getCaption());
        post.setLikeCount((int) photo.getLikeCount());
        post.setCommentCount((int) photo.getCommentCount());
        post.setShareCount((int) photo.getShareCount());

        // These will be set based on current user if available
        if (currentUser != null) {
            // For now, set defaults - could add isLiked/isSaved check here
            post.setLikedByCurrentUser(false);
            post.setSavedByCurrentUser(false);
        }

        return post;
    }

    private UnifiedPostResponse convertShareToUnifiedPost(Share share, Photo originalPhoto, 
                                                          User sharerUser, Map<String, User> userMap) {
        UnifiedPostResponse post = new UnifiedPostResponse();
        post.setId("share_" + share.getId());  // Prefix to distinguish from photos
        post.setType(UnifiedPostResponse.PostType.SHARE);
        post.setCreatedAt(share.getCreatedAt());

        // Sharer info
        if (sharerUser != null) {
            post.setUserId(sharerUser.getId());
            post.setUsername(sharerUser.getUsername());
            post.setUserImageUrl(userAvatarCacheService.getAvatar(sharerUser.getId()));
        }

        // Share caption
        post.setShareCaption(share.getCaption());

        // Original photo info
        post.setOriginalPhotoId(originalPhoto.getId());
        post.setOriginalImageUrl(originalPhoto.getImageUrl());
        post.setOriginalCaption(originalPhoto.getCaption());
        post.setOriginalCreatedAt(originalPhoto.getCreatedAt());
        post.setOriginalLikeCount((int) originalPhoto.getLikeCount());
        post.setOriginalCommentCount((int) originalPhoto.getCommentCount());
        post.setOriginalShareCount((int) originalPhoto.getShareCount());

        if (originalPhoto.getUser() != null) {
            post.setOriginalUsername(originalPhoto.getUser().getUsername());
            User originalUser = userMap.get(originalPhoto.getUser().getUserId());
            if (originalUser != null) {
                post.setOriginalUserImageUrl(userAvatarCacheService.getAvatar(originalUser.getId()));
            }
        }

        return post;
    }
}

