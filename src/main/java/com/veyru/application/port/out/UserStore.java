package com.veyru.application.port.out;

import com.veyru.application.common.PageQuery;
import com.veyru.application.common.PageResult;
import com.veyru.domain.model.User;
import java.util.List;
import java.util.Optional;

public interface UserStore {
  User save(User user);

  Optional<User> findById(String id);

  Optional<User> findByEmail(String email);

  Optional<User> findByUsername(String username);

  Optional<User> findByPhoneNumber(String phoneNumber);

  Optional<User> findByResetToken(String token);

  boolean existsByEmail(String email);

  boolean existsByUsername(String username);

  boolean existsByPhoneNumber(String phoneNumber);

  List<User> findAllById(List<String> ids);

  default List<User> findByIdIn(List<String> ids) {
    return findAllById(ids);
  }

  List<User> findAll();

  PageResult<User> findAll(PageQuery page);

  PageResult<User> searchByName(String term, PageQuery page);

  PageResult<User> searchByUsername(String term, PageQuery page);

  void incrementPhotoCount(String userId, long delta);

  void incrementFollowerCount(String userId, long delta);

  void incrementFollowingCount(String userId, long delta);
}
