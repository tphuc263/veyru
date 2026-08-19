package com.veyru.dto.response.user;

import java.util.HashMap;
import lombok.Data;

@Data
public class UserProfileResponse {
  private String id;
  private String username;
  private String imageUrl;
  private HashMap<String, Long> stats;
  private String bio;
  private boolean isFollowingByCurrentUser;
}
