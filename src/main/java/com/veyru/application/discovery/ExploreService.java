package com.veyru.application.discovery;

import com.veyru.application.common.PageQuery;
import com.veyru.application.common.PageResult;
import com.veyru.application.identity.UserProfileService;
import com.veyru.application.media.PhotoConversionService;
import com.veyru.application.port.out.FollowStore;
import com.veyru.application.port.out.PhotoStore;
import com.veyru.application.result.photo.PhotoResponse;
import com.veyru.domain.model.Follow;
import com.veyru.domain.model.Photo;
import com.veyru.domain.model.User;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class ExploreService {
  private final PhotoStore photos;
  private final PhotoConversionService conversion;
  private final UserProfileService users;
  private final FollowStore follows;

  public PageResult<PhotoResponse> getExploreFeed(String userId, int page, int size) {
    User actor = users.findUserById(userId);
    List<String> excluded = new ArrayList<>();
    excluded.add(userId);
    excluded.addAll(follows.findByFollowerId(userId).stream().map(Follow::getFollowingId).toList());
    PageResult<Photo> result =
        photos.explore(
            excluded, Instant.now().minus(Duration.ofDays(30)), new PageQuery(page, size));
    if (result.items().isEmpty() && page == 0) return getPopularPhotos(page, size);
    return result.map(photo -> conversion.convertToPhotoResponse(photo, actor));
  }

  public PageResult<PhotoResponse> getPopularPhotos(int page, int size) {
    User actor = users.getCurrentUser();
    return photos
        .popular(new PageQuery(page, size))
        .map(photo -> conversion.convertToPhotoResponse(photo, actor));
  }

  public PageResult<PhotoResponse> getPhotosByTag(String tag, int page, int size) {
    User actor = users.getCurrentUser();
    return photos
        .findByTags(List.of(tag.toLowerCase()), new PageQuery(page, size))
        .map(photo -> conversion.convertToPhotoResponse(photo, actor));
  }

  public ExploreService(
      PhotoStore photos,
      PhotoConversionService conversion,
      UserProfileService users,
      FollowStore follows) {
    this.photos = photos;
    this.conversion = conversion;
    this.users = users;
    this.follows = follows;
  }
}
