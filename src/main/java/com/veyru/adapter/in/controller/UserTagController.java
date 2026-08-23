package com.veyru.adapter.in.controller;

import com.veyru.adapter.in.dto.request.usertag.CreateUserTagRequest;
import com.veyru.application.result.usertag.UserTagResponse;
import com.veyru.application.social.CreateUserTagCommand;
import com.veyru.application.social.UserTagService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("${api.prefix}")
public class UserTagController {
  private final UserTagService userTagService;

  @PostMapping("/photos/{photoId}/user-tags")
  public ResponseEntity<UserTagResponse> tagUserInPhoto(
      @PathVariable String photoId, @Valid @RequestBody CreateUserTagRequest request) {
    UserTagResponse response =
        userTagService.tagUserInPhoto(
            photoId,
            new CreateUserTagCommand(
                request.taggedUserId(), request.positionX(), request.positionY()));
    return ResponseEntity.status(201).body(response);
  }

  @DeleteMapping("/photos/{photoId}/user-tags/{taggedUserId}")
  public ResponseEntity<Void> removeUserTag(
      @PathVariable String photoId, @PathVariable String taggedUserId) {
    userTagService.removeUserTag(photoId, taggedUserId);
    return ResponseEntity.noContent().build();
  }

  @GetMapping("/photos/{photoId}/user-tags")
  public ResponseEntity<List<UserTagResponse>> getPhotoUserTags(@PathVariable String photoId) {
    List<UserTagResponse> tags = userTagService.getPhotoUserTags(photoId);
    return ResponseEntity.ok(tags);
  }

  @GetMapping("/users/{userId}/photo-tags")
  public ResponseEntity<List<UserTagResponse>> getPhotosWhereUserIsTagged(
      @PathVariable String userId) {
    List<UserTagResponse> tags = userTagService.getPhotosWhereUserIsTagged(userId);
    return ResponseEntity.ok(tags);
  }

  public UserTagController(final UserTagService userTagService) {
    this.userTagService = userTagService;
  }
}
