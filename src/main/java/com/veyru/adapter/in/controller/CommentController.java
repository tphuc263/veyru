package com.veyru.adapter.in.controller;

import com.veyru.adapter.in.dto.request.comment.CreateCommentRequest;
import com.veyru.adapter.in.dto.request.comment.UpdateCommentRequest;
import com.veyru.application.result.comment.CommentResponse;
import com.veyru.application.social.CommentService;
import com.veyru.application.social.CreateCommentCommand;
import com.veyru.application.social.UpdateCommentCommand;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("${api.prefix}")
public class CommentController {
  private final CommentService commentService;

  // Create a comment on a photo (supports nested comments via parentCommentId in request body)
  @PostMapping("/photos/{photoId}/comments")
  public ResponseEntity<CommentResponse> createComment(
      @PathVariable String photoId, @Valid @RequestBody CreateCommentRequest request) {
    CommentResponse comment =
        commentService.createComment(
            photoId,
            new CreateCommentCommand(
                request.text(), request.parentCommentId(), request.mentionedUsernames()));
    return ResponseEntity.status(201).body(comment);
  }

  // Update a comment
  @PatchMapping("/comments/{commentId}")
  public ResponseEntity<CommentResponse> updateComment(
      @PathVariable String commentId, @Valid @RequestBody UpdateCommentRequest request) {
    CommentResponse comment =
        commentService.updateComment(commentId, new UpdateCommentCommand(request.text()));
    return ResponseEntity.ok(comment);
  }

  // Delete a comment
  @DeleteMapping("/comments/{commentId}")
  public ResponseEntity<Void> deleteComment(@PathVariable String commentId) {
    commentService.deleteComment(commentId);
    return ResponseEntity.noContent().build();
  }

  // Get all comments for a photo (top-level comments with nested replies)
  @GetMapping("/photos/{photoId}/comments")
  public ResponseEntity<List<CommentResponse>> getPhotoComments(@PathVariable String photoId) {
    List<CommentResponse> comments = commentService.getPhotoComments(photoId);
    return ResponseEntity.ok(comments);
  }

  // Get replies for a specific comment
  @GetMapping("/comments/{commentId}/replies")
  public ResponseEntity<List<CommentResponse>> getCommentReplies(
      @PathVariable String commentId,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size) {
    List<CommentResponse> replies = commentService.getCommentReplies(commentId, page, size);
    return ResponseEntity.ok(replies);
  }

  // Get comments count for a photo
  @GetMapping("/photos/{photoId}/comments/count")
  public ResponseEntity<Long> getPhotoCommentsCount(@PathVariable String photoId) {
    long count = commentService.getPhotoCommentsCount(photoId);
    return ResponseEntity.ok(count);
  }

  // Get a specific comment
  @GetMapping("/comments/{commentId}")
  public ResponseEntity<CommentResponse> getComment(@PathVariable String commentId) {
    CommentResponse comment = commentService.getComment(commentId);
    return ResponseEntity.ok(comment);
  }

  @PutMapping("/comments/{commentId}/likes/me")
  public ResponseEntity<CommentResponse> likeComment(@PathVariable String commentId) {
    CommentResponse comment = commentService.likeComment(commentId);
    return ResponseEntity.ok(comment);
  }

  @DeleteMapping("/comments/{commentId}/likes/me")
  public ResponseEntity<Void> unlikeComment(@PathVariable String commentId) {
    commentService.unlikeComment(commentId);
    return ResponseEntity.noContent().build();
  }

  public CommentController(final CommentService commentService) {
    this.commentService = commentService;
  }
}
