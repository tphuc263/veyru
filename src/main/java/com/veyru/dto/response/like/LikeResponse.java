package com.veyru.dto.response.like;

import java.time.Instant;
import lombok.Data;

@Data
public class LikeResponse {
  private String id;
  private String userId;
  private String username;
  private String userImageUrl;
  private Instant createdAt;
}
