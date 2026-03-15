package share_app.tphucshareapp.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import share_app.tphucshareapp.dto.response.ApiResponse;
import share_app.tphucshareapp.dto.response.follow.FollowResponse;
import share_app.tphucshareapp.service.follow.IFollowService;

import java.util.List;

@RestController
@RequestMapping("${api.prefix}/follows")
@RequiredArgsConstructor
public class FollowController {
    private final IFollowService followService;

    // Follow
    @PostMapping("/follow/{targetUserId}")
    public ResponseEntity<ApiResponse<Void>> follow(@PathVariable String targetUserId) {
        followService.follow(targetUserId);
        return ResponseEntity.ok(ApiResponse.success(null, "Follow status toggled successfully"));
    }

    // unfollow
    @PostMapping("/unfollow/{targetUserId}")
    public ResponseEntity<ApiResponse<Void>> unfollow(@PathVariable String targetUserId) {
        followService.unfollow(targetUserId);
        return ResponseEntity.ok(ApiResponse.success(null, "Follow status toggled successfully"));
    }

    // Get followers of a user  
    @GetMapping("/{userId}/followers")
    public ResponseEntity<ApiResponse<List<FollowResponse>>> getFollowers(
            @PathVariable String userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        List<FollowResponse> followers = followService.getFollowers(userId, page, size);
        return ResponseEntity.ok(ApiResponse.success(followers, "Followers retrieved successfully"));
    }

    // Get users that a user is following
    @GetMapping("/{userId}/following")
    public ResponseEntity<ApiResponse<List<FollowResponse>>> getFollowing(
            @PathVariable String userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        List<FollowResponse> following = followService.getFollowing(userId, page, size);
        return ResponseEntity.ok(ApiResponse.success(following, "Following list retrieved successfully"));
    }

    // Check if user A follows user B
    @GetMapping("/check/{followerId}/{followingId}")
    public ResponseEntity<ApiResponse<Boolean>> checkFollowStatus(
            @PathVariable String followerId,
            @PathVariable String followingId) {
        boolean isFollowing = followService.isFollowing(followerId, followingId);
        return ResponseEntity.ok(ApiResponse.success(isFollowing, "Follow status checked successfully"));
    }
}
