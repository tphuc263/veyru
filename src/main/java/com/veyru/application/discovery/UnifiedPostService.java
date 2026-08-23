package com.veyru.application.discovery;

import com.veyru.application.common.PageQuery;
import com.veyru.application.common.PageResult;
import com.veyru.application.identity.UserProfileService;
import com.veyru.application.port.out.AvatarCache;
import com.veyru.application.port.out.PhotoStore;
import com.veyru.application.port.out.ShareStore;
import com.veyru.application.port.out.UserStore;
import com.veyru.application.result.post.UnifiedPostResponse;
import com.veyru.domain.model.Photo;
import com.veyru.domain.model.Share;
import com.veyru.domain.model.User;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UnifiedPostService {
  private static final Logger log = LoggerFactory.getLogger(UnifiedPostService.class);
  private final PhotoStore photoStore;
  private final ShareStore shareStore;
  private final UserStore userStore;
  private final UserProfileService userService;
  private final AvatarCache userAvatarCacheService;

  /**
   * Get unified posts (photos + shares) for a user's profile Sorted by createdAt descending (newest
   * first)
   */
  public PageResult<UnifiedPostResponse> getUserPosts(String userId, int page, int size) {
    log.info("Fetching unified posts for user: {}", userId);
    // Fetch photos
    PageResult<Photo> photosPage = photoStore.findByUser(userId, new PageQuery(page, size));
    List<Photo> photos = photosPage.items();
    // Fetch shares
    List<Share> shares = shareStore.findByUserId(userId, page, size);
    // Convert to unified posts
    List<UnifiedPostResponse> allPosts = new ArrayList<>();
    // Add photos
    User currentUser = null;

    currentUser = userService.getCurrentUser();

    // Not logged in, that's okay
    for (Photo photo : photos) {
      UnifiedPostResponse post = convertPhotoToUnifiedPost(photo, currentUser);
      allPosts.add(post);
    }
    // Add shares
    Map<String, Photo> photoMap =
        photoStore.findAllById(shares.stream().map(Share::getPhotoId).distinct().toList()).stream()
            .collect(Collectors.toMap(Photo::getId, p -> p));
    // Get original users
    List<String> originalUserIds =
        photoMap.values().stream()
            .map(p -> p.getUser() != null ? p.getUser().getUserId() : null)
            .filter(id -> id != null)
            .distinct()
            .toList();
    Map<String, User> userMap =
        userStore.findByIdIn(originalUserIds).stream()
            .collect(Collectors.toMap(User::getId, u -> u));
    // Get sharer info
    User sharerUser = userStore.findById(userId).orElse(null);
    for (Share share : shares) {
      Photo originalPhoto = photoMap.get(share.getPhotoId());
      if (originalPhoto != null) {
        UnifiedPostResponse post =
            convertShareToUnifiedPost(share, originalPhoto, sharerUser, userMap);
        allPosts.add(post);
      }
    }
    // Sort by createdAt descending
    allPosts.sort(Comparator.comparing(UnifiedPostResponse::getCreatedAt).reversed());
    // Paginate
    int start = page * size;
    int end = Math.min(start + size, allPosts.size());
    if (start >= allPosts.size()) {
      return new PageResult<>(
          List.of(), page, size, allPosts.size(), (int) Math.ceil((double) allPosts.size() / size));
    }
    List<UnifiedPostResponse> pagePosts = allPosts.subList(start, end);
    return new PageResult<>(
        pagePosts, page, size, allPosts.size(), (int) Math.ceil((double) allPosts.size() / size));
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

  private UnifiedPostResponse convertShareToUnifiedPost(
      Share share, Photo originalPhoto, User sharerUser, Map<String, User> userMap) {
    UnifiedPostResponse post = new UnifiedPostResponse();
    post.setId("share_" + share.getId()); // Prefix to distinguish from photos
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

  public UnifiedPostService(
      final PhotoStore photoStore,
      final ShareStore shareStore,
      final UserStore userStore,
      final UserProfileService userService,
      final AvatarCache userAvatarCacheService) {
    this.photoStore = photoStore;
    this.shareStore = shareStore;
    this.userStore = userStore;
    this.userService = userService;
    this.userAvatarCacheService = userAvatarCacheService;
  }
}
