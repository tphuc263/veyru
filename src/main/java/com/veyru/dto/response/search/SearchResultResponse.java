package com.veyru.dto.response.search;

import lombok.Data;
import com.veyru.dto.response.photo.PhotoResponse;
import com.veyru.dto.response.user.UserProfileResponse;

import java.util.List;

@Data
public class SearchResultResponse {
    private List<UserProfileResponse> users;
    private List<PhotoResponse> photos;
    private long totalUsers;
    private long totalPhotos;
    private String query;
}
