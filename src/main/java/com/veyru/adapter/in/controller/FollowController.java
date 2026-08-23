package com.veyru.adapter.in.controller;

import com.veyru.application.result.follow.FollowResponse;
import com.veyru.application.social.FollowService;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("${api.prefix}")
public class FollowController {
  private final FollowService followService;

  // Follow
  @PutMapping("/users/me/following/{targetUserId}")
  public ResponseEntity<Void> follow(@PathVariable String targetUserId) {
    followService.follow(targetUserId);
    return ResponseEntity.noContent().build();
  }

  // unfollow
  @DeleteMapping("/users/me/following/{targetUserId}")
  public ResponseEntity<Void> unfollow(@PathVariable String targetUserId) {
    followService.unfollow(targetUserId);
    return ResponseEntity.noContent().build();
  }

  // Get followers of a user
  @GetMapping("/users/{userId}/followers")
  public ResponseEntity<List<FollowResponse>> getFollowers(
      @PathVariable String userId,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size) {
    List<FollowResponse> followers = followService.getFollowers(userId, page, size);
    return ResponseEntity.ok(followers);
  }

  // Get users that a user is following
  @GetMapping("/users/{userId}/following")
  public ResponseEntity<List<FollowResponse>> getFollowing(
      @PathVariable String userId,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size) {
    List<FollowResponse> following = followService.getFollowing(userId, page, size);
    return ResponseEntity.ok(following);
  }

  // Check if user A follows user B
  @GetMapping("/users/me/following/{followingId}")
  public ResponseEntity<Boolean> checkFollowStatus(@PathVariable String followingId) {
    boolean isFollowing = followService.isFollowing(null, followingId);
    return ResponseEntity.ok(isFollowing);
  }

  public FollowController(final FollowService followService) {
    this.followService = followService;
  }
}
