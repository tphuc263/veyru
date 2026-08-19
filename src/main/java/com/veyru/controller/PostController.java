package com.veyru.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.veyru.dto.response.ApiResponse;
import com.veyru.dto.response.post.UnifiedPostResponse;
import com.veyru.service.post.UnifiedPostService;

@RestController
@RequestMapping("${api.prefix}/posts")
@RequiredArgsConstructor
public class PostController {

    private final UnifiedPostService unifiedPostService;

    /**
     * Get unified posts (photos + shares) for a user profile
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<Page<UnifiedPostResponse>>> getUserPosts(
            @PathVariable String userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<UnifiedPostResponse> posts = unifiedPostService.getUserPosts(userId, page, size);
        return ResponseEntity.ok(ApiResponse.success(posts, "User posts retrieved successfully"));
    }
}

