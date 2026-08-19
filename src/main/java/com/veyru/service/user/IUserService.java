package com.veyru.service.user;

import com.veyru.dto.request.user.UpdateProfileRequest;
import com.veyru.dto.response.user.UserProfileResponse;
import org.springframework.data.domain.Page;

public interface IUserService {
  UserProfileResponse getUserProfileById(String userId);

  UserProfileResponse getCurrentUserProfile();

  UserProfileResponse updateProfile(UpdateProfileRequest request);

  Page<UserProfileResponse> getAllUsers(int page, int size);
}
