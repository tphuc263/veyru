package com.veyru.dto.response.follow;

import lombok.Data;

@Data
public class FollowStatsResponse {
    private long followersCount;
    private long followingCount;
    private boolean isFollowedByCurrentUser;
}