package com.veyru.application.identity;

import com.veyru.application.common.PageQuery;
import com.veyru.application.common.PageResult;
import com.veyru.application.common.error.UseCaseError;
import com.veyru.application.common.error.UseCaseException;
import com.veyru.application.port.out.AvatarCache;
import com.veyru.application.port.out.CurrentActor;
import com.veyru.application.port.out.ImageStorage;
import com.veyru.application.port.out.UserStore;
import com.veyru.application.result.user.UserProfileResult;
import com.veyru.application.social.FollowService;
import com.veyru.domain.model.User;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class UserProfileService {
  private final UserStore userStore;
  private final ImageStorage imageStorage;
  private final FollowService followService;
  private final AvatarCache userAvatarCacheService;
  private final CurrentActor currentActor;

  public UserProfileResult getUserProfileById(String targetUserId) {
    User targetUser = findUserById(targetUserId);
    HashMap<String, Long> stats = new HashMap<>();
    stats.put("posts", targetUser.photoCount());
    stats.put("followers", targetUser.followerCount());
    stats.put("following", targetUser.followingCount());
    boolean following =
        findCurrentUser()
            .map(currentUser -> followService.isFollowing(currentUser.id(), targetUserId))
            .orElse(false);
    return new UserProfileResult(
        targetUser.id(),
        targetUser.username(),
        targetUser.imageUrl(),
        stats,
        targetUser.bio(),
        following);
  }

  public UserProfileResult getCurrentUserProfile() {
    User user = requireCurrentUser();
    HashMap<String, Long> stats = new HashMap<>();
    stats.put("posts", user.photoCount());
    stats.put("followers", user.followerCount());
    stats.put("following", user.followingCount());
    return new UserProfileResult(
        user.id(), user.username(), user.imageUrl(), stats, user.bio(), false);
  }

  public UserProfileResult updateProfile(UpdateProfileCommand request) {
    User user = requireCurrentUser();
    String oldImageUrl = user.imageUrl();
    user = user.withUpdatedProfile(request.username(), request.bio(), null);
    if (request.image() != null && !request.image().isEmpty()) {
      String newImageUrl = imageStorage.upload(request.image());
      user = user.withUpdatedProfile(null, null, newImageUrl);
    }
    User updatedUser = userStore.save(user);
    if (!java.util.Objects.equals(updatedUser.imageUrl(), oldImageUrl)) {
      userAvatarCacheService.updateAvatar(updatedUser.id(), updatedUser.imageUrl());
      if (oldImageUrl != null && !oldImageUrl.isEmpty()) {
        imageStorage.deleteByUrl(oldImageUrl);
      }
    }
    return mapToUserProfileResponse(updatedUser);
  }

  public PageResult<UserProfileResult> getAllUsers(int page, int size) {
    PageResult<User> users = userStore.findAll(new PageQuery(page, size));
    return users.map(this::mapToUserProfileResponse);
  }

  public User requireCurrentUser() {
    return findUserById(
        currentActor
            .id()
            .orElseThrow(() -> new UseCaseException(UseCaseError.AUTHENTICATION_REQUIRED)));
  }

  public Optional<User> findCurrentUser() {
    return currentActor.id().flatMap(userStore::findById);
  }

  public String requireCurrentUserId() {
    return currentActor
        .id()
        .orElseThrow(() -> new UseCaseException(UseCaseError.AUTHENTICATION_REQUIRED));
  }

  public Optional<String> findCurrentUserId() {
    return currentActor.id();
  }

  public User findUserById(String userId) {
    return userStore
        .findById(userId)
        .orElseThrow(() -> new UseCaseException(UseCaseError.RESOURCE_NOT_FOUND));
  }

  private UserProfileResult mapToUserProfileResponse(User user) {
    return new UserProfileResult(
        user.id(), user.username(), user.imageUrl(), null, user.bio(), false);
  }

  public Map<String, User> findUsersByIds(List<String> userIds) {
    if (userIds == null || userIds.isEmpty()) {
      return Map.of();
    }
    return userStore.findAllById(userIds).stream()
        .collect(Collectors.toMap(User::id, user -> user));
  }

  public UserProfileService(
      final UserStore userStore,
      final ImageStorage imageStorage,
      final FollowService followService,
      final AvatarCache userAvatarCacheService,
      final CurrentActor currentActor) {
    this.userStore = userStore;
    this.imageStorage = imageStorage;
    this.followService = followService;
    this.userAvatarCacheService = userAvatarCacheService;
    this.currentActor = currentActor;
  }
}
