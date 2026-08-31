package com.veyru.application.discovery;

import com.veyru.application.common.PageQuery;
import com.veyru.application.common.PageResult;
import com.veyru.application.port.out.AvatarCache;
import com.veyru.application.port.out.PhotoStore;
import com.veyru.application.port.out.ShareStore;
import com.veyru.application.port.out.UserStore;
import com.veyru.application.result.post.UnifiedPostResult;
import com.veyru.domain.model.Photo;
import com.veyru.domain.model.Share;
import com.veyru.domain.model.User;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class UnifiedPostService {
  private final PhotoStore photoStore;
  private final ShareStore shareStore;
  private final UserStore userStore;
  private final AvatarCache userAvatarCacheService;

  public PageResult<UnifiedPostResult> getUserPosts(String userId, int page, int size) {
    PageResult<Photo> photosPage = photoStore.findByUser(userId, new PageQuery(page, size));
    List<Photo> photos = photosPage.items();
    List<Share> shares = shareStore.findByUserId(userId, page, size);
    List<UnifiedPostResult> allPosts = new ArrayList<>();
    for (Photo photo : photos) {
      UnifiedPostResult post = convertPhotoToUnifiedPost(photo);
      allPosts.add(post);
    }
    Map<String, Photo> photoMap =
        photoStore.findAllById(shares.stream().map(Share::photoId).distinct().toList()).stream()
            .collect(Collectors.toMap(Photo::id, p -> p));
    List<String> originalUserIds =
        photoMap.values().stream().map(photo -> photo.author().userId()).distinct().toList();
    Map<String, User> userMap =
        userStore.findAllById(originalUserIds).stream().collect(Collectors.toMap(User::id, u -> u));
    User sharerUser = userStore.findById(userId).orElse(null);
    for (Share share : shares) {
      Photo originalPhoto = photoMap.get(share.photoId());
      if (originalPhoto != null) {
        UnifiedPostResult post =
            convertShareToUnifiedPost(share, originalPhoto, sharerUser, userMap);
        allPosts.add(post);
      }
    }
    allPosts.sort(Comparator.comparing(UnifiedPostResult::getCreatedAt).reversed());
    int start = page * size;
    int end = Math.min(start + size, allPosts.size());
    if (start >= allPosts.size()) {
      return new PageResult<>(
          List.of(), page, size, allPosts.size(), (int) Math.ceil((double) allPosts.size() / size));
    }
    List<UnifiedPostResult> pagePosts = allPosts.subList(start, end);
    return new PageResult<>(
        pagePosts, page, size, allPosts.size(), (int) Math.ceil((double) allPosts.size() / size));
  }

  private UnifiedPostResult convertPhotoToUnifiedPost(Photo photo) {
    UnifiedPostResult post = new UnifiedPostResult();
    post.setId(photo.id());
    post.setType(UnifiedPostResult.PostType.PHOTO);
    post.setCreatedAt(photo.createdAt());
    post.setUserId(photo.author().userId());
    post.setUsername(photo.author().username());
    post.setUserImageUrl(userAvatarCacheService.getAvatar(photo.author().userId()));
    post.setImageUrl(photo.imageUrl());
    post.setCaption(photo.caption());
    post.setLikeCount((int) photo.likeCount());
    post.setCommentCount((int) photo.commentCount());
    post.setShareCount((int) photo.shareCount());
    post.setLikedByCurrentUser(false);
    post.setSavedByCurrentUser(false);
    return post;
  }

  private UnifiedPostResult convertShareToUnifiedPost(
      Share share, Photo originalPhoto, User sharerUser, Map<String, User> userMap) {
    UnifiedPostResult post = new UnifiedPostResult();
    post.setId("share_" + share.id());
    post.setType(UnifiedPostResult.PostType.SHARE);
    post.setCreatedAt(share.createdAt());
    if (sharerUser != null) {
      post.setUserId(sharerUser.id());
      post.setUsername(sharerUser.username());
      post.setUserImageUrl(userAvatarCacheService.getAvatar(sharerUser.id()));
    }
    post.setShareCaption(share.caption());
    post.setOriginalPhotoId(originalPhoto.id());
    post.setOriginalImageUrl(originalPhoto.imageUrl());
    post.setOriginalCaption(originalPhoto.caption());
    post.setOriginalCreatedAt(originalPhoto.createdAt());
    post.setOriginalLikeCount((int) originalPhoto.likeCount());
    post.setOriginalCommentCount((int) originalPhoto.commentCount());
    post.setOriginalShareCount((int) originalPhoto.shareCount());
    post.setOriginalUsername(originalPhoto.author().username());
    User originalUser = userMap.get(originalPhoto.author().userId());
    if (originalUser != null) {
      post.setOriginalUserImageUrl(userAvatarCacheService.getAvatar(originalUser.id()));
    }
    return post;
  }

  public UnifiedPostService(
      final PhotoStore photoStore,
      final ShareStore shareStore,
      final UserStore userStore,
      final AvatarCache userAvatarCacheService) {
    this.photoStore = photoStore;
    this.shareStore = shareStore;
    this.userStore = userStore;
    this.userAvatarCacheService = userAvatarCacheService;
  }
}
