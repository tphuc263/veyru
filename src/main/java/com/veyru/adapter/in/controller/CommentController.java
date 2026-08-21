package com.veyru.adapter.in.controller;

import com.veyru.adapter.in.dto.request.comment.CreateCommentRequest;
import com.veyru.adapter.in.dto.request.comment.UpdateCommentRequest;
import com.veyru.adapter.in.dto.response.comment.CommentResponse;
import com.veyru.domain.service.comment.CommentService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("${api.prefix}/comments")
public class CommentController {
  private final CommentService commentService;

  // Create a comment on a photo (supports nested comments via parentCommentId in request body)
  @PostMapping("/photo/{photoId}")
  public ResponseEntity<CommentResponse> createComment(
      @PathVariable String photoId, @Valid @RequestBody CreateCommentRequest request) {
    CommentResponse comment = commentService.createComment(photoId, request);
    return ResponseEntity.status(201).body(comment);
  }

  // Update a comment
  @PutMapping("/{commentId}")
  public ResponseEntity<CommentResponse> updateComment(
      @PathVariable String commentId, @Valid @RequestBody UpdateCommentRequest request) {
    CommentResponse comment = commentService.updateComment(commentId, request);
    return ResponseEntity.ok(comment);
  }

  // Delete a comment
  @DeleteMapping("/{commentId}")
  public ResponseEntity<Void> deleteComment(@PathVariable String commentId) {
    commentService.deleteComment(commentId);
    return ResponseEntity.noContent().build();
  }

  // Get all comments for a photo (top-level comments with nested replies)
  @GetMapping("/photo/{photoId}")
  public ResponseEntity<List<CommentResponse>> getPhotoComments(@PathVariable String photoId) {
    List<CommentResponse> comments = commentService.getPhotoComments(photoId);
    return ResponseEntity.ok(comments);
  }

  // Get replies for a specific comment
  @GetMapping("/{commentId}/replies")
  public ResponseEntity<List<CommentResponse>> getCommentReplies(
      @PathVariable String commentId,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size) {
    List<CommentResponse> replies = commentService.getCommentReplies(commentId, page, size);
    return ResponseEntity.ok(replies);
  }

  // Get comments count for a photo
  @GetMapping("/photo/{photoId}/count")
  public ResponseEntity<Long> getPhotoCommentsCount(@PathVariable String photoId) {
    long count = commentService.getPhotoCommentsCount(photoId);
    return ResponseEntity.ok(count);
  }

  // Get a specific comment
  @GetMapping("/{commentId}")
  public ResponseEntity<CommentResponse> getComment(@PathVariable String commentId) {
    CommentResponse comment = commentService.getComment(commentId);
    return ResponseEntity.ok(comment);
  }

  // Toggle like on a comment
  @PostMapping("/{commentId}/like")
  public ResponseEntity<CommentResponse> toggleCommentLike(@PathVariable String commentId) {
    CommentResponse comment = commentService.toggleCommentLike(commentId);
    return ResponseEntity.ok(comment);
  }

  public CommentController(final CommentService commentService) {
    this.commentService = commentService;
  }
}
