package com.veyru.adapter.in.controller;

import com.veyru.adapter.in.dto.request.usertag.CreateUserTagRequest;
import com.veyru.adapter.in.dto.response.usertag.UserTagResponse;
import com.veyru.domain.service.usertag.UserTagService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("${api.prefix}/user-tags")
public class UserTagController {
  private final UserTagService userTagService;

  @PostMapping("/photo/{photoId}")
  public ResponseEntity<UserTagResponse> tagUserInPhoto(
      @PathVariable String photoId, @Valid @RequestBody CreateUserTagRequest request) {
    UserTagResponse response = userTagService.tagUserInPhoto(photoId, request);
    return ResponseEntity.status(201).body(response);
  }

  @DeleteMapping("/photo/{photoId}/user/{taggedUserId}")
  public ResponseEntity<Void> removeUserTag(
      @PathVariable String photoId, @PathVariable String taggedUserId) {
    userTagService.removeUserTag(photoId, taggedUserId);
    return ResponseEntity.noContent().build();
  }

  @GetMapping("/photo/{photoId}")
  public ResponseEntity<List<UserTagResponse>> getPhotoUserTags(@PathVariable String photoId) {
    List<UserTagResponse> tags = userTagService.getPhotoUserTags(photoId);
    return ResponseEntity.ok(tags);
  }

  @GetMapping("/user/{userId}/tagged-photos")
  public ResponseEntity<List<UserTagResponse>> getPhotosWhereUserIsTagged(
      @PathVariable String userId) {
    List<UserTagResponse> tags = userTagService.getPhotosWhereUserIsTagged(userId);
    return ResponseEntity.ok(tags);
  }

  public UserTagController(final UserTagService userTagService) {
    this.userTagService = userTagService;
  }
}
