package com.veyru.adapter.in.web;

import com.veyru.adapter.in.dto.request.user.UpdateProfileRequest;
import com.veyru.adapter.in.dto.response.PageResponse;
import com.veyru.adapter.in.dto.response.user.UserProfileResponse;
import com.veyru.application.common.ImageFile;
import com.veyru.application.common.PageResult;
import com.veyru.application.identity.UpdateProfileCommand;
import com.veyru.application.identity.UserProfileService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("${api.prefix}/users")
@Validated
public class UserController {
  private static final Logger log = LoggerFactory.getLogger(UserController.class);
  private final UserProfileService userService;

  @GetMapping
  public ResponseEntity<PageResponse<UserProfileResponse>> getAllUsers(
      @RequestParam(defaultValue = "0") @Min(0) int page,
      @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
    var users = userService.getAllUsers(page, size);
    return ResponseEntity.ok(PageResponse.from(users, UserProfileResponse::from));
  }

  @GetMapping("/{userId}")
  public ResponseEntity<UserProfileResponse> getUserProfile(@PathVariable String userId) {
    return ResponseEntity.ok(UserProfileResponse.from(userService.getUserProfileById(userId)));
  }

  @GetMapping("/me")
  public ResponseEntity<UserProfileResponse> getCurrentUserProfile() {
    return ResponseEntity.ok(UserProfileResponse.from(userService.getCurrentUserProfile()));
  }

  @PatchMapping(value = "/me", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<UserProfileResponse> updateProfile(
      @Valid @ModelAttribute UpdateProfileRequest request) throws IOException {
    var file = request.image();
    ImageFile image =
        file == null
            ? null
            : new ImageFile(file.getBytes(), file.getOriginalFilename(), file.getContentType());
    return ResponseEntity.ok(
        UserProfileResponse.from(
            userService.updateProfile(
                new UpdateProfileCommand(request.username(), request.bio(), image))));
  }

  public UserController(final UserProfileService userService) {
    this.userService = userService;
  }
}
