package com.veyru.dto.response.follow;

import lombok.Data;

@Data
public class FollowResponse {
  private String id;
  private String userId;
  private String username;
  private String userImageUrl;
  private String firstName;
  private String lastName;
  private String bio;
  private boolean isFollowedByCurrentUser;
}
