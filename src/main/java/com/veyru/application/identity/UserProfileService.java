package com.veyru.application.identity;

import com.veyru.application.common.PageQuery;
import com.veyru.application.common.PageResult;
import com.veyru.application.port.out.AvatarCache;
import com.veyru.application.port.out.CurrentActor;
import com.veyru.application.port.out.ImageStorage;
import com.veyru.application.port.out.UserStore;
import com.veyru.application.result.user.UserProfileResponse;
import com.veyru.application.social.FollowService;
import com.veyru.domain.exception.ApiException;
import com.veyru.domain.exception.ErrorCode;
import com.veyru.domain.model.User;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserProfileService {
  private static final Logger log = LoggerFactory.getLogger(UserProfileService.class);
  private final UserStore userStore;
  private final ImageStorage imageStorage;
  private final FollowService followService;
  private final AvatarCache userAvatarCacheService;
  private final CurrentActor currentActor;

  public UserProfileResponse getUserProfileById(String targetUserId) {
    User currentUser = getCurrentUser();
    User targetUser = findUserById(targetUserId);
    UserProfileResponse response = mapToUserProfileResponse(targetUser);
    if (followService.isFollowing(currentUser.getId(), targetUserId)) {
      response.setFollowingByCurrentUser(true);
    }
    HashMap<String, Long> stats = new HashMap<>();
    stats.put("posts", targetUser.getPhotoCount());
    stats.put("followers", targetUser.getFollowerCount());
    stats.put("following", targetUser.getFollowingCount());
    response.setStats(stats);
    return response;
  }

  public UserProfileResponse getCurrentUserProfile() {
    User user = getCurrentUser();
    UserProfileResponse response = mapToUserProfileResponse(user);
    HashMap<String, Long> stats = new HashMap<>();
    stats.put("posts", user.getPhotoCount());
    stats.put("followers", user.getFollowerCount());
    stats.put("following", user.getFollowingCount());
    response.setStats(stats);
    return response;
  }

  public UserProfileResponse updateProfile(UpdateProfileCommand request) {
    User user = getCurrentUser();
    String oldImageUrl = user.getImageUrl();
    updateUserFields(user, request);
    // Handle image update if provided
    if (request.image() != null && !request.image().isEmpty()) {

      // Delete old image if exists
      if (oldImageUrl != null && !oldImageUrl.isEmpty()) {
        imageStorage.deleteByUrl(oldImageUrl);
      }
      // Upload new imag
      String newImageUrl = imageStorage.upload(request.image());
      user.setImageUrl(newImageUrl);
      log.info("New profile image uploaded for user ID: {}", user.getId());
    }
    User updatedUser = userStore.save(user);
    // Update avatar cache if image changed
    if (updatedUser.getImageUrl() != null && !updatedUser.getImageUrl().equals(oldImageUrl)) {
      userAvatarCacheService.updateAvatar(updatedUser.getId(), updatedUser.getImageUrl());
    }
    log.info("User profile updated successfully for user ID: {}", updatedUser.getId());
    return mapToUserProfileResponse(updatedUser);
  }

  public PageResult<UserProfileResponse> getAllUsers(int page, int size) {
    log.info("Fetching all users - page: {}, size: {}", page, size);
    PageResult<User> users = userStore.findAll(new PageQuery(page, size));
    return users.map(this::mapToUserProfileResponse);
  }

  // helper methods
  public User getCurrentUser() {
    return findUserById(
        currentActor.id().orElseThrow(() -> new ApiException(ErrorCode.AUTHENTICATION_REQUIRED)));
  }

  public User findUserById(String userId) {
    return userStore
        .findById(userId)
        .orElseThrow(
            () -> {
              log.error("User not found with ID: {}", userId);
              return new ApiException(ErrorCode.RESOURCE_NOT_FOUND);
            });
  }

  private void updateUserFields(User user, UpdateProfileCommand request) {
    if (request.username() != null) {
      user.setUsername(request.username());
    }
    if (request.bio() != null) {
      user.setBio(request.bio());
    }
  }

  private UserProfileResponse mapToUserProfileResponse(User user) {
    UserProfileResponse response = new UserProfileResponse();
    response.setId(user.getId());
    response.setUsername(user.getUsername());
    response.setImageUrl(user.getImageUrl());
    response.setBio(user.getBio());
    return response;
  }

  public Map<String, User> findUsersByIds(List<String> userIds) {
    if (userIds == null || userIds.isEmpty()) {
      return Map.of();
    }
    return userStore.findAllById(userIds).stream()
        .collect(Collectors.toMap(User::getId, user -> user));
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
