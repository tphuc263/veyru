package com.veyru.service.user;

import org.springframework.data.domain.Page;
import com.veyru.dto.request.user.UpdateProfileRequest;
import com.veyru.dto.response.user.UserProfileResponse;

public interface IUserService {
    UserProfileResponse getUserProfileById(String userId);

    UserProfileResponse getCurrentUserProfile();

    UserProfileResponse updateProfile(UpdateProfileRequest request);

    Page<UserProfileResponse> getAllUsers(int page, int size);
}
