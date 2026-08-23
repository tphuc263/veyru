package com.veyru.application.discovery;

import com.veyru.application.common.PageQuery;
import com.veyru.application.common.PageResult;
import com.veyru.application.identity.UserProfileService;
import com.veyru.application.media.PhotoConversionService;
import com.veyru.application.port.out.FollowStore;
import com.veyru.application.port.out.PhotoStore;
import com.veyru.application.result.photo.PhotoResult;
import com.veyru.domain.model.Follow;
import com.veyru.domain.model.Photo;
import java.time.Clock;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class ExploreService {
  private final PhotoStore photos;
  private final PhotoConversionService conversion;
  private final UserProfileService users;
  private final FollowStore follows;
  private final Clock clock;

  public PageResult<PhotoResult> getExploreFeed(String userId, int page, int size) {
    var actor = users.findCurrentUser();
    List<String> excluded = new ArrayList<>();
    actor.ifPresent(user -> excluded.add(user.getId()));
    actor.ifPresent(
        user ->
            excluded.addAll(
                follows.findByFollowerId(user.getId()).stream()
                    .map(Follow::getFollowingId)
                    .toList()));
    PageResult<Photo> result =
        photos.explore(
            excluded, clock.instant().minus(Duration.ofDays(30)), new PageQuery(page, size));
    if (result.items().isEmpty() && page == 0) return getPopularPhotos(page, size);
    return result.map(photo -> conversion.convertToPhotoResponse(photo, actor));
  }

  public PageResult<PhotoResult> getPopularPhotos(int page, int size) {
    var actor = users.findCurrentUser();
    return photos
        .popular(new PageQuery(page, size))
        .map(photo -> conversion.convertToPhotoResponse(photo, actor));
  }

  public PageResult<PhotoResult> getPhotosByTag(String tag, int page, int size) {
    var actor = users.findCurrentUser();
    return photos
        .findByTags(List.of(tag.toLowerCase()), new PageQuery(page, size))
        .map(photo -> conversion.convertToPhotoResponse(photo, actor));
  }

  public ExploreService(
      PhotoStore photos,
      PhotoConversionService conversion,
      UserProfileService users,
      FollowStore follows,
      Clock clock) {
    this.photos = photos;
    this.conversion = conversion;
    this.users = users;
    this.follows = follows;
    this.clock = clock;
  }
}
