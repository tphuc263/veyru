package com.veyru.domain.service.search;

import com.veyru.adapter.in.dto.response.photo.PhotoResponse;
import com.veyru.adapter.in.dto.response.search.UserSearchResponse;
import com.veyru.adapter.in.dto.response.search.UserSearchResponseSimple;
import com.veyru.adapter.in.dto.response.user.UserProfileResponse;
import com.veyru.application.port.out.PhotoRepository;
import com.veyru.application.port.out.UserRepository;
import com.veyru.domain.model.Photo;
import com.veyru.domain.model.User;
import com.veyru.domain.service.follow.FollowService;
import com.veyru.domain.service.photo.PhotoConversionService;
import com.veyru.domain.service.user.UserService;
import java.util.List;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
public class SearchService {
  private static final Logger log = LoggerFactory.getLogger(SearchService.class);
  private final UserRepository userRepository;
  private final PhotoRepository photoRepository;
  private final PhotoConversionService photoConversionService;
  private final UserService userService;
  private final FollowService followService;

  public Page<UserSearchResponseSimple> searchUsers(String query, int page, int size) {
    log.info("Searching users for: {}", query);
    String sanitizedQuery = sanitizeSearchQuery(query);
    if (sanitizedQuery.isEmpty()) {
      return Page.empty();
    }
    Pageable pageable = PageRequest.of(page, size);
    // Search by username, firstName, or lastName
    Page<User> users = userRepository.findByNameFields(sanitizedQuery, pageable);
    log.info("Found {} users matching query: {}", users.getTotalElements(), query);
    return users.map(this::toSimpleResponse);
  }

  public Page<PhotoResponse> searchPhotos(String query, int page, int size) {
    log.info("Searching photos for: {}", query);
    String sanitizedQuery = sanitizeSearchQuery(query);
    if (sanitizedQuery.isEmpty()) {
      return Page.empty();
    }
    Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
    User currentUser = userService.getCurrentUser();
    // Try text search first

    Page<Photo> photos = photoRepository.findByTextSearch(sanitizedQuery, pageable);
    if (!photos.isEmpty()) {
      return photos.map(photo -> photoConversionService.convertToPhotoResponse(photo, currentUser));
    }

    // Fallback to caption search
    return photoRepository
        .findByCaptionContainingIgnoreCase(sanitizedQuery, pageable)
        .map(photo -> photoConversionService.convertToPhotoResponse(photo, currentUser));
  }

  public Page<PhotoResponse> searchPhotosByTags(String query, int page, int size) {
    log.info("Searching photos by tags for: {}", query);
    String sanitizedQuery = sanitizeSearchQuery(query);
    if (sanitizedQuery.isEmpty()) {
      return Page.empty();
    }
    // 1. Find tags that match the query
    // Assuming the query is a space-separated string of tags, e.g., "nature sky"
    List<String> tagNames = List.of(sanitizedQuery.split("\\s+"));
    if (tagNames.isEmpty()) {
      return Page.empty();
    }
    // 2. Find Photos that contain these tags directly
    Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
    User currentUser = null;

    currentUser = userService.getCurrentUser();

    final User finalCurrentUser = currentUser;
    Page<Photo> photos = photoRepository.findByTagsIn(tagNames, pageable);
    return photos.map(
        photo -> photoConversionService.convertToPhotoResponse(photo, finalCurrentUser));
  }

  public List<String> getSearchSuggestions(String query, int limit) {
    log.info("Getting search suggestions for: {}", query);
    String sanitizedQuery = sanitizeSearchQuery(query);
    if (sanitizedQuery.isEmpty()) {
      return List.of();
    }
    Set<String> suggestions = new java.util.HashSet<>();
    // Get user suggestions

    List<User> users =
        userRepository.findByNameFields(sanitizedQuery, PageRequest.of(0, limit)).getContent();
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

  private UserSearchResponse convertToUserSearchResponse(User user) {
    UserSearchResponse response = new UserSearchResponse();
    response.setId(user.getId());
    response.setUsername(user.getUsername());
    response.setImageUrl(user.getImageUrl());
    response.setBio(user.getBio());
    // Add follower count
    response.setFollowersCount(user.getFollowerCount());
    // Check if current user follows this user

    User currentUser = userService.getCurrentUser();
    response.setFollowedByCurrentUser(followService.isFollowing(currentUser.getId(), user.getId()));

    return response;
  }

  private UserProfileResponse convertToUserProfileResponse(UserSearchResponse userSearchResponse) {
    UserProfileResponse response = new UserProfileResponse();
    response.setId(userSearchResponse.getId());
    response.setUsername(userSearchResponse.getUsername());
    response.setImageUrl(userSearchResponse.getImageUrl());
    response.setBio(userSearchResponse.getBio());
    return response;
  }

  private UserSearchResponseSimple toSimpleResponse(User user) {
    UserSearchResponseSimple response = new UserSearchResponseSimple();
    response.setId(user.getId());
    response.setUsername(user.getUsername());
    response.setImageUrl(user.getImageUrl());
    return response;
  }

  public SearchService(
      final UserRepository userRepository,
      final PhotoRepository photoRepository,
      final PhotoConversionService photoConversionService,
      final UserService userService,
      final FollowService followService) {
    this.userRepository = userRepository;
    this.photoRepository = photoRepository;
    this.photoConversionService = photoConversionService;
    this.userService = userService;
    this.followService = followService;
  }
}
