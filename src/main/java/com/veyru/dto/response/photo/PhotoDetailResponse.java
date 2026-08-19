package com.veyru.dto.response.photo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import com.veyru.dto.response.comment.CommentResponse;
import com.veyru.dto.response.like.LikeResponse;

import java.time.Instant;
import java.util.List;

@Data
public class PhotoDetailResponse {
    private String id;
    private String userId;
    private String username;
    private String userImageUrl;
    private String imageUrl;
    private String caption;
    private Instant createdAt;
    private int likeCount;
    private int commentCount;
    private int shareCount;

    @JsonProperty("isLikedByCurrentUser")
    private boolean isLikedByCurrentUser;

    @JsonProperty("isSavedByCurrentUser")
    private boolean isSavedByCurrentUser;

    private List<LikeResponse> likes;
    private List<CommentResponse> comments;
    private List<String> tags;
}
