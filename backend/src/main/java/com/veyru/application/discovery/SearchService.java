package com.veyru.application.discovery;

import com.veyru.application.common.PageQuery;
import com.veyru.application.common.PageResult;
import com.veyru.application.identity.UserProfileService;
import com.veyru.application.media.PhotoConversionService;
import com.veyru.application.media.PhotoViewer;
import com.veyru.application.port.out.PhotoStore;
import com.veyru.application.port.out.UserStore;
import com.veyru.application.result.photo.PhotoResult;
import com.veyru.application.result.search.UserSearchSimpleResult;
import com.veyru.domain.model.Photo;
import com.veyru.domain.model.User;
import java.util.List;
import java.util.Set;

public class SearchService {
  private final UserStore userStore;
  private final PhotoStore photoStore;
  private final PhotoConversionService photoConversionService;
  private final UserProfileService userService;

  public PageResult<UserSearchSimpleResult> searchUsers(String query, int page, int size) {
    String sanitizedQuery = sanitizeSearchQuery(query);
    if (sanitizedQuery.isEmpty()) {
      return new PageResult<>(List.of(), page, size, 0, 0);
    }
    PageResult<User> users = userStore.searchByName(sanitizedQuery, new PageQuery(page, size));
    return users.map(this::toSimpleResponse);
  }

  public PageResult<PhotoResult> searchPhotos(String query, int page, int size) {
    String sanitizedQuery = sanitizeSearchQuery(query);
    if (sanitizedQuery.isEmpty()) {
      return new PageResult<>(List.of(), page, size, 0, 0);
    }
    PhotoViewer viewer = currentPhotoViewer();
    PageResult<Photo> photos = photoStore.searchText(sanitizedQuery, new PageQuery(page, size));
    return photos.map(photo -> photoConversionService.convertToPhotoResponse(photo, viewer));
  }

  public PageResult<PhotoResult> searchPhotosByTags(String query, int page, int size) {
    String sanitizedQuery = sanitizeSearchQuery(query);
    if (sanitizedQuery.isEmpty()) {
      return new PageResult<>(List.of(), page, size, 0, 0);
    }
    List<String> tagNames = List.of(sanitizedQuery.split("\\s+"));
    PhotoViewer viewer = currentPhotoViewer();
    PageResult<Photo> photos = photoStore.findByTags(tagNames, new PageQuery(page, size));
    return photos.map(photo -> photoConversionService.convertToPhotoResponse(photo, viewer));
  }

  public List<String> getSearchSuggestions(String query, int limit) {
    String sanitizedQuery = sanitizeSearchQuery(query);
    if (sanitizedQuery.isEmpty()) {
      return List.of();
    }
    Set<String> suggestions = new java.util.HashSet<>();
    List<User> users = userStore.searchByName(sanitizedQuery, new PageQuery(0, limit)).items();
    users.forEach(
        user -> {
          suggestions.add(user.username());
        });

    return suggestions.stream().limit(limit).sorted().toList();
  }

  private String sanitizeSearchQuery(String query) {
    if (query == null) return "";
    return query.trim().replaceAll("[\"\'`]", "").replaceAll("\\s+", " ");
  }

  private UserSearchSimpleResult toSimpleResponse(User user) {
    return new UserSearchSimpleResult(user.id(), user.username(), user.imageUrl());
  }

  private PhotoViewer currentPhotoViewer() {
    return userService
        .findCurrentUser()
        .<PhotoViewer>map(PhotoViewer::authenticated)
        .orElseGet(PhotoViewer::anonymous);
  }

  public SearchService(
      final UserStore userStore,
      final PhotoStore photoStore,
      final PhotoConversionService photoConversionService,
      final UserProfileService userService) {
    this.userStore = userStore;
    this.photoStore = photoStore;
    this.photoConversionService = photoConversionService;
    this.userService = userService;
  }
}
