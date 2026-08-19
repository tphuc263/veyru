package com.veyru.dto.response.usertag;

import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserTagResponse {
  private String id;
  private String photoId;
  private String taggedUserId;
  private String taggedByUserId;
  private String username;
  private String userImageUrl;
  private Double positionX;
  private Double positionY;
  private Instant createdAt;
}
