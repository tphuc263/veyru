package com.veyru.controller;

import com.veyru.dto.request.user.UpdateProfileRequest;
import com.veyru.dto.response.PageResponse;
import com.veyru.dto.response.user.UserProfileResponse;
import com.veyru.service.user.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("${api.prefix}/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {
  private final UserService userService;

  @GetMapping("/all")
  public ResponseEntity<PageResponse<UserProfileResponse>> getAllUsers(
      @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
    Page<UserProfileResponse> users = userService.getAllUsers(page, size);
    return ResponseEntity.ok(PageResponse.from(users));
  }

  @GetMapping("/{userId}")
  public ResponseEntity<UserProfileResponse> getUserProfile(@PathVariable String userId) {
    UserProfileResponse profile = userService.getUserProfileById(userId);
    return ResponseEntity.ok(profile);
  }

  @GetMapping("/me")
  public ResponseEntity<UserProfileResponse> getCurrentUserProfile() {
    UserProfileResponse profile = userService.getCurrentUserProfile();
    return ResponseEntity.ok(profile);
  }

  @PutMapping(value = "/me", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<UserProfileResponse> updateProfile(
      @ModelAttribute UpdateProfileRequest request) {
    UserProfileResponse updatedProfile = userService.updateProfile(request);
    return ResponseEntity.ok(updatedProfile);
  }
}
