package com.veyru.service.usertag;

import com.veyru.dto.request.usertag.CreateUserTagRequest;
import com.veyru.dto.response.usertag.UserTagResponse;

import java.util.List;

public interface IUserTagService {
    UserTagResponse tagUserInPhoto(String photoId, CreateUserTagRequest request);
    
    void removeUserTag(String photoId, String taggedUserId);
    
    List<UserTagResponse> getPhotoUserTags(String photoId);
    
    List<UserTagResponse> getPhotosWhereUserIsTagged(String userId);
}
