package com.veyru.application.port.out;

import com.veyru.application.common.PageQuery;
import com.veyru.application.common.PageResult;
import com.veyru.domain.model.Photo;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface PhotoStore {
  Photo save(Photo photo);

  Optional<Photo> findById(String id);

  List<Photo> findAll();

  List<Photo> findAllById(List<String> ids);

  PageResult<Photo> findAll(PageQuery page);

  PageResult<Photo> findByUser(String userId, PageQuery page);

  PageResult<Photo> searchText(String text, PageQuery page);

  PageResult<Photo> searchCaption(String text, PageQuery page);

  PageResult<Photo> findByTags(List<String> tags, PageQuery page);

  List<Photo> findByUsersAfter(List<String> userIds, Instant after);

  List<Photo> findByUsersBetween(List<String> userIds, Instant after, Instant before, int limit);

  List<Photo> findByUsersBefore(List<String> userIds, Instant before, int limit);

  List<Photo> findByUsers(List<String> userIds);

  List<Photo> findByUser(String userId);

  List<Photo> findTaggedUser(String userId);

  PageResult<Photo> explore(List<String> excludedUserIds, Instant after, PageQuery page);

  PageResult<Photo> popular(PageQuery page);

  long count();

  void deleteById(String id);

  void incrementLikeCount(String id, long delta);

  void incrementCommentCount(String id, long delta);

  void incrementShareCount(String id, long delta);

  void addUserTag(String id, Photo.EmbeddedUserTag tag);

  void removeUserTag(String id, String userId);
}
