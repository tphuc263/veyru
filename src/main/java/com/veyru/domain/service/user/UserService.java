package com.veyru.domain.service.user;

import com.veyru.adapter.in.dto.request.user.UpdateProfileRequest;
import com.veyru.adapter.in.dto.response.user.UserProfileResponse;
import com.veyru.domain.exception.ApiException;
import com.veyru.domain.exception.ErrorCode;
import com.veyru.domain.model.User;
import com.veyru.application.port.out.UserRepository;
import com.veyru.adapter.in.security.userdetails.AppUserDetails;
import com.veyru.domain.service.follow.FollowService;
import com.veyru.domain.service.photo.CloudinaryService;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class UserService {
  private static final Logger log = LoggerFactory.getLogger(UserService.class);
  private final UserRepository userRepository;
  private final CloudinaryService cloudinaryService;
  private final FollowService followService;
  private final UserAvatarCacheService userAvatarCacheService;

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

  public UserProfileResponse updateProfile(UpdateProfileRequest request) {
    User user = getCurrentUser();
    String oldImageUrl = user.getImageUrl();
    updateUserFields(user, request);
    // Handle image update if provided
    if (request.getImage() != null && !request.getImage().isEmpty()) {
      try {
        // Delete old image if exists
        if (oldImageUrl != null && !oldImageUrl.isEmpty()) {
          String publicId = cloudinaryService.extractPublicIdFromUrl(oldImageUrl);
          if (publicId != null) {
            cloudinaryService.deleteImage(publicId);
            log.info("Old profile image deleted for user ID: {}", user.getId());
          }
        }
        // Upload new imag
        Map<String, Object> uploadResult = cloudinaryService.uploadImage(request.getImage());
        String newImageUrl = (String) uploadResult.get("secure_url");
        user.setImageUrl(newImageUrl);
        log.info("New profile image uploaded for user ID: {}", user.getId());
      } catch (Exception e) {
        log.error("Failed to update profile image for user ID: {}", user.getId(), e);
        throw new RuntimeException("Failed to update profile image", e);
      }
    }
    User updatedUser = userRepository.save(user);
    // Update avatar cache if image changed
    if (updatedUser.getImageUrl() != null && !updatedUser.getImageUrl().equals(oldImageUrl)) {
      userAvatarCacheService.updateAvatar(updatedUser.getId(), updatedUser.getImageUrl());
    }
    log.info("User profile updated successfully for user ID: {}", updatedUser.getId());
    return mapToUserProfileResponse(updatedUser);
  }

  public Page<UserProfileResponse> getAllUsers(int page, int size) {
    log.info("Fetching all users - page: {}, size: {}", page, size);
    Pageable pageable = PageRequest.of(page, size);
    Page<User> users = userRepository.findAll(pageable);
    return users.map(this::mapToUserProfileResponse);
  }

  // helper methods
  public User getCurrentUser() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (!isUserAuthenticated(authentication)) {
      log.warn(
          "User not authenticated properly. Authentication: {}",
          authentication != null ? authentication.getClass().getSimpleName() : "null");
      throw new ApiException(ErrorCode.AUTHENTICATION_REQUIRED);
    }
    AppUserDetails userDetails = (AppUserDetails) authentication.getPrincipal();
    return findUserById(userDetails.getId());
  }

  private boolean isUserAuthenticated(Authentication authentication) {
    return authentication != null
        && authentication.isAuthenticated()
        && authentication.getPrincipal() instanceof AppUserDetails;
  }

  public User findUserById(String userId) {
    return userRepository
        .findById(userId)
        .orElseThrow(
            () -> {
              log.error("User not found with ID: {}", userId);
              return new ApiException(ErrorCode.RESOURCE_NOT_FOUND);
            });
  }

  private void updateUserFields(User user, UpdateProfileRequest request) {
    if (request.getUsername() != null) {
      user.setUsername(request.getUsername());
    }
    if (request.getBio() != null) {
      user.setBio(request.getBio());
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
    return userRepository.findAllById(userIds).stream()
        .collect(Collectors.toMap(User::getId, user -> user));
  }

  public UserService(
      final UserRepository userRepository,
      final CloudinaryService cloudinaryService,
      final FollowService followService,
      final UserAvatarCacheService userAvatarCacheService) {
    this.userRepository = userRepository;
    this.cloudinaryService = cloudinaryService;
    this.followService = followService;
    this.userAvatarCacheService = userAvatarCacheService;
  }
}
