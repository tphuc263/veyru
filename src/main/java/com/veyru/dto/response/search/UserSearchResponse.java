package com.veyru.dto.response.search;

import lombok.Data;

@Data
public class UserSearchResponse {
    private String id;
    private String username;
    private String firstName;
    private String lastName;
    private String imageUrl;
    private String bio;
    private long followersCount;
    private boolean isFollowedByCurrentUser;
    private double searchScore;
}