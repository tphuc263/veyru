package com.veyru.dto.response.share;

import java.time.Instant;
import lombok.Data;

@Data
public class ShareResponse {
  private String id;
  private String photoId;
  private String userId;
  private String username;
  private String userImageUrl;
  private String caption;
  private Instant createdAt;
}
