package com.veyru.dto.response.like;

import lombok.Data;

import java.time.Instant;

@Data
public class LikeResponse {
    private String id;
    private String userId;
    private String username;
    private String userImageUrl;
    private Instant createdAt;
}
