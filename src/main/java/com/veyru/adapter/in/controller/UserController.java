package com.veyru.adapter.in.controller;

import com.veyru.adapter.in.dto.request.user.UpdateProfileRequest;
import com.veyru.adapter.in.dto.response.PageResponse;
import com.veyru.adapter.in.dto.response.user.UserProfileResponse;
import com.veyru.domain.service.user.UserService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("${api.prefix}/users")
@Validated
public class UserController {
  private static final Logger log = LoggerFactory.getLogger(UserController.class);
  private final UserService userService;

  @GetMapping
  public ResponseEntity<PageResponse<UserProfileResponse>> getAllUsers(
      @RequestParam(defaultValue = "0") @Min(0) int page,
      @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
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

  @PatchMapping(value = "/me", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<UserProfileResponse> updateProfile(
      @Valid @ModelAttribute UpdateProfileRequest request) {
    UserProfileResponse updatedProfile = userService.updateProfile(request);
    return ResponseEntity.ok(updatedProfile);
  }

  public UserController(final UserService userService) {
    this.userService = userService;
  }
}
