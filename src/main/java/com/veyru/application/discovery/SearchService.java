package com.veyru.application.discovery;

import com.veyru.application.common.PageQuery;
import com.veyru.application.common.PageResult;
import com.veyru.application.identity.UserProfileService;
import com.veyru.application.media.PhotoConversionService;
import com.veyru.application.port.out.PhotoStore;
import com.veyru.application.port.out.UserStore;
import com.veyru.application.result.photo.PhotoResult;
import com.veyru.application.result.search.UserSearchResult;
import com.veyru.application.result.search.UserSearchSimpleResult;
import com.veyru.application.result.user.UserProfileResult;
import com.veyru.application.social.FollowService;
import com.veyru.domain.model.Photo;
import com.veyru.domain.model.User;
import java.util.List;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SearchService {
  private static final Logger log = LoggerFactory.getLogger(SearchService.class);
  private final UserStore userStore;
  private final PhotoStore photoStore;
  private final PhotoConversionService photoConversionService;
  private final UserProfileService userService;
  private final FollowService followService;

  public PageResult<UserSearchSimpleResult> searchUsers(String query, int page, int size) {
    log.info("Searching users for: {}", query);
    String sanitizedQuery = sanitizeSearchQuery(query);
    if (sanitizedQuery.isEmpty()) {
      return new PageResult<>(List.of(), page, size, 0, 0);
    }
    // Search by username, firstName, or lastName
    PageResult<User> users = userStore.searchByName(sanitizedQuery, new PageQuery(page, size));
    log.info("Found {} users matching query: {}", users.totalElements(), query);
    return users.map(this::toSimpleResponse);
  }

  public PageResult<PhotoResult> searchPhotos(String query, int page, int size) {
    log.info("Searching photos for: {}", query);
    String sanitizedQuery = sanitizeSearchQuery(query);
    if (sanitizedQuery.isEmpty()) {
      return new PageResult<>(List.of(), page, size, 0, 0);
    }
    var currentUser = userService.findCurrentUser();
    // Try text search first

    PageResult<Photo> photos = photoStore.searchText(sanitizedQuery, new PageQuery(page, size));
    if (!photos.items().isEmpty()) {
      return photos.map(photo -> photoConversionService.convertToPhotoResponse(photo, currentUser));
    }

    // Fallback to caption search
    return photoStore
        .searchCaption(sanitizedQuery, new PageQuery(page, size))
        .map(photo -> photoConversionService.convertToPhotoResponse(photo, currentUser));
  }

  public PageResult<PhotoResult> searchPhotosByTags(String query, int page, int size) {
    log.info("Searching photos by tags for: {}", query);
    String sanitizedQuery = sanitizeSearchQuery(query);
    if (sanitizedQuery.isEmpty()) {
      return new PageResult<>(List.of(), page, size, 0, 0);
    }
    // 1. Find tags that match the query
    // Assuming the query is a space-separated string of tags, e.g., "nature sky"
    List<String> tagNames = List.of(sanitizedQuery.split("\\s+"));
    if (tagNames.isEmpty()) {
      return new PageResult<>(List.of(), page, size, 0, 0);
    }
    // 2. Find Photos that contain these tags directly
    var currentUser = userService.findCurrentUser();
    PageResult<Photo> photos = photoStore.findByTags(tagNames, new PageQuery(page, size));
    return photos.map(
        photo -> photoConversionService.convertToPhotoResponse(photo, currentUser));
  }

  public List<String> getSearchSuggestions(String query, int limit) {
    log.info("Getting search suggestions for: {}", query);
    String sanitizedQuery = sanitizeSearchQuery(query);
    if (sanitizedQuery.isEmpty()) {
      return List.of();
    }
    Set<String> suggestions = new java.util.HashSet<>();
    // Get user suggestions

    List<User> users = userStore.searchByName(sanitizedQuery, new PageQuery(0, limit)).items();
    users.forEach(
        user -> {
          suggestions.add(user.getUsername());
        });

    return suggestions.stream().limit(limit).sorted().toList();
  }

  // Helper methods
  private String sanitizeSearchQuery(String query) {
    if (query == null) return "";
    // Remove special characters that might interfere with search
    return // Remove quotes
    query.trim().replaceAll("[\"\'`]", "").replaceAll("\\s+", " "); // Normalize whitespace
  }

  private UserSearchResult convertToUserSearchResponse(User user) {
    return new UserSearchResult(
        user.getId(), user.getUsername(), null, null, user.getImageUrl(), user.getBio(),
        user.getFollowerCount(),
        userService.findCurrentUser()
            .map(currentUser -> followService.isFollowing(currentUser.getId(), user.getId()))
            .orElse(false),
        0.0);
  }

  private UserProfileResult convertToUserProfileResponse(UserSearchResult userSearchResponse) {
    return new UserProfileResult(
        userSearchResponse.getId(), userSearchResponse.getUsername(),
        userSearchResponse.getImageUrl(), null, userSearchResponse.getBio(), false);
  }

  private UserSearchSimpleResult toSimpleResponse(User user) {
    return new UserSearchSimpleResult(user.getId(), user.getUsername(), user.getImageUrl());
  }

  public SearchService(
      final UserStore userStore,
      final PhotoStore photoStore,
      final PhotoConversionService photoConversionService,
      final UserProfileService userService,
      final FollowService followService) {
    this.userStore = userStore;
    this.photoStore = photoStore;
    this.photoConversionService = photoConversionService;
    this.userService = userService;
    this.followService = followService;
  }
}
