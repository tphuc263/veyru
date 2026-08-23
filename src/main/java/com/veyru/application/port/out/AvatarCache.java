package com.veyru.application.port.out;

import java.util.List;
import java.util.Map;

public interface AvatarCache {
  String getAvatar(String userId);

  Map<String, String> getAvatars(List<String> userIds);

  void updateAvatar(String userId, String imageUrl);

  void evictAvatar(String userId);
}
