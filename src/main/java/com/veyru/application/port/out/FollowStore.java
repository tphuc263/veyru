package com.veyru.application.port.out;

import com.veyru.domain.model.Follow;
import java.util.List;
import java.util.Optional;

public interface FollowStore {
  Follow save(Follow follow);

  void delete(Follow follow);

  Optional<Follow> find(String followerId, String followingId);

  boolean exists(String followerId, String followingId);

  List<Follow> findFollowers(String userId, int page, int size);

  List<Follow> findFollowing(String userId, int page, int size);

  List<Follow> findByFollowerId(String userId);

  List<Follow> findByFollowingId(String userId);

  List<Follow> findAll();
}
