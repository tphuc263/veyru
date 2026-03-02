package share_app.tphucshareapp.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import share_app.tphucshareapp.dto.request.comment.CreateCommentRequest;
import share_app.tphucshareapp.dto.request.comment.UpdateCommentRequest;
import share_app.tphucshareapp.dto.response.ApiResponse;
import share_app.tphucshareapp.dto.response.comment.CommentResponse;
import share_app.tphucshareapp.service.comment.CommentService;

import java.util.List;

@RestController
@RequestMapping("${api.prefix}/comments")
@RequiredArgsConstructor
public class CommentController {
    private final CommentService commentService;

    // Create a comment on a photo
    @PostMapping("/photo/{photoId}")
    public ResponseEntity<ApiResponse<CommentResponse>> createComment(
            @PathVariable String photoId,
            @Valid @RequestBody CreateCommentRequest request) {
        CommentResponse comment = commentService.createComment(photoId, request);
        return ResponseEntity.ok(ApiResponse.success(comment, "Comment created successfully"));
    }

    // Update a comment
    @PutMapping("/{commentId}")
    public ResponseEntity<ApiResponse<CommentResponse>> updateComment(
            @PathVariable String commentId,
            @Valid @RequestBody UpdateCommentRequest request) {
        CommentResponse comment = commentService.updateComment(commentId, request);
        return ResponseEntity.ok(ApiResponse.success(comment, "Comment updated successfully"));
    }

    // Delete a comment
    @DeleteMapping("/{commentId}")
    public ResponseEntity<ApiResponse<Void>> deleteComment(@PathVariable String commentId) {
        commentService.deleteComment(commentId);
        return ResponseEntity.ok(ApiResponse.success(null, "Comment deleted successfully"));
    }

    // Get all comments for a photo -> pagnition ?
    @GetMapping("/photo/{photoId}")
    public ResponseEntity<ApiResponse<List<CommentResponse>>> getPhotoComments(
            @PathVariable String photoId) {
        List<CommentResponse> comments = commentService.getPhotoComments(photoId);
        return ResponseEntity.ok(ApiResponse.success(comments, "Photo comments retrieved successfully"));
    }

    // Get comments count for a photo
    @GetMapping("/photo/{photoId}/count")
    public ResponseEntity<ApiResponse<Long>> getPhotoCommentsCount(@PathVariable String photoId) {
        long count = commentService.getPhotoCommentsCount(photoId);
        return ResponseEntity.ok(ApiResponse.success(count, "Photo comments count retrieved successfully"));
    }

    // Get a specific comment
    @GetMapping("/{commentId}")
    public ResponseEntity<ApiResponse<CommentResponse>> getComment(@PathVariable String commentId) {
        CommentResponse comment = commentService.getComment(commentId);
        return ResponseEntity.ok(ApiResponse.success(comment, "Comment retrieved successfully"));
    }
}
