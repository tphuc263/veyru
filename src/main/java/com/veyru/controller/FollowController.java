package com.veyru.controller;

import com.veyru.dto.response.follow.FollowResponse;
import com.veyru.service.follow.FollowService;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("${api.prefix}/follows")
public class FollowController {
  private final FollowService followService;

  // Follow
  @PostMapping("/follow/{targetUserId}")
  public ResponseEntity<Void> follow(@PathVariable String targetUserId) {
    followService.follow(targetUserId);
    return ResponseEntity.status(201).build();
  }

  // unfollow
  @PostMapping("/unfollow/{targetUserId}")
  public ResponseEntity<Void> unfollow(@PathVariable String targetUserId) {
    followService.unfollow(targetUserId);
    return ResponseEntity.noContent().build();
  }

  // Get followers of a user
  @GetMapping("/{userId}/followers")
  public ResponseEntity<List<FollowResponse>> getFollowers(
      @PathVariable String userId,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size) {
    List<FollowResponse> followers = followService.getFollowers(userId, page, size);
    return ResponseEntity.ok(followers);
  }

  // Get users that a user is following
  @GetMapping("/{userId}/following")
  public ResponseEntity<List<FollowResponse>> getFollowing(
      @PathVariable String userId,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size) {
    List<FollowResponse> following = followService.getFollowing(userId, page, size);
    return ResponseEntity.ok(following);
  }

  // Check if user A follows user B
  @GetMapping("/check/{followerId}/{followingId}")
  public ResponseEntity<Boolean> checkFollowStatus(
      @PathVariable String followerId, @PathVariable String followingId) {
    boolean isFollowing = followService.isFollowing(followerId, followingId);
    return ResponseEntity.ok(isFollowing);
  }

  public FollowController(final FollowService followService) {
    this.followService = followService;
  }
}
