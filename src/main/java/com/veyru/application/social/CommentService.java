package com.veyru.application.social;

import com.veyru.application.identity.UserProfileService;
import com.veyru.application.notification.NotificationService;
import com.veyru.application.port.out.AvatarCache;
import com.veyru.application.port.out.CommentLikeStore;
import com.veyru.application.port.out.CommentStore;
import com.veyru.application.port.out.GraphProjection;
import com.veyru.application.port.out.PhotoStore;
import com.veyru.application.port.out.UserStore;
import com.veyru.application.result.comment.CommentResponse;
import com.veyru.domain.exception.ApiException;
import com.veyru.domain.exception.ErrorCode;
import com.veyru.domain.model.Comment;
import com.veyru.domain.model.CommentLike;
import com.veyru.domain.model.Photo;
import com.veyru.domain.model.User;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CommentService {
  private static final Logger log = LoggerFactory.getLogger(CommentService.class);
  private final CommentStore commentStore;
  private final CommentLikeStore commentLikeStore;
  private final PhotoStore photoStore;
  private final UserStore userStore;
  private final UserProfileService userService;
  private final NotificationService notificationService;
  private final AvatarCache userAvatarCacheService;
  private final GraphProjection neo4jGraphService;
  // Pattern to match @username mentions
  private static final Pattern MENTION_PATTERN = Pattern.compile("@(\\w+)");

  public CommentResponse createComment(String photoId, CreateCommentCommand request) {
    // Validate photo exists
    Photo photo =
        photoStore
            .findById(photoId)
            .orElseThrow(() -> new ApiException(ErrorCode.RESOURCE_NOT_FOUND));
    User currentUser = userService.getCurrentUser();
    Comment.EmbeddedUser embeddedUser = new Comment.EmbeddedUser();
    embeddedUser.setUserId(currentUser.getId());
    embeddedUser.setUsername(currentUser.getUsername());
    // Create comment
    Comment comment = new Comment();
    comment.setPhotoId(photoId);
    comment.setUserId(currentUser.getId());
    comment.setText(request.text());
    comment.setCreatedAt(Instant.now());
    comment.setUser(embeddedUser);
    comment.setLikeCount(0);
    comment.setReplyCount(0);
    // Handle nested comments (replies)
    String parentCommentId = request.parentCommentId();
    Comment parentComment = null;
    if (parentCommentId != null && !parentCommentId.isEmpty()) {
      parentComment =
          commentStore
              .findById(parentCommentId)
              .orElseThrow(() -> new ApiException(ErrorCode.RESOURCE_NOT_FOUND));
      comment.setParentCommentId(parentCommentId);
    }
    // Extract mentioned users from text
    List<String> mentionedUserIds = extractMentionedUserIds(request.text());
    comment.setMentionedUserIds(mentionedUserIds);
    Comment savedComment = commentStore.save(comment);
    // Update parent comment reply count if this is a reply
    if (parentComment != null) {
      commentStore.incrementReplyCount(parentCommentId, 1);
      // Send notification for reply
      notificationService.sendReplyCommentNotification(
          parentComment.getUserId(),
          currentUser,
          photoId,
          savedComment.getId(),
          photo.getImageUrl());
    } else {
      // Only increment photo comment count for top-level comments
      photoStore.incrementCommentCount(photoId, 1);
    }
    // Send notification to photo owner for new comment (only for top-level
    // comments)
    if (parentComment == null && photo.getUser() != null) {
      notificationService.sendCommentPhotoNotification(
          photo.getUser().getUserId(),
          currentUser,
          photoId,
          savedComment.getId(),
          photo.getImageUrl());
    }
    // Send notifications to mentioned users
    for (String mentionedUserId : mentionedUserIds) {
      notificationService.sendMentionNotification(
          mentionedUserId, currentUser, photoId, savedComment.getId(), photo.getImageUrl());
    }
    // Sync to Neo4j graph - create comment relationship

    neo4jGraphService.createCommentRelationship(currentUser.getId(), photoId);

    log.info("Comment created successfully by user {} on photo {}", currentUser.getId(), photoId);
    return convertToCommentResponse(savedComment, currentUser.getId());
  }

  public CommentResponse updateComment(String commentId, UpdateCommentCommand request) {
    Comment comment =
        commentStore
            .findById(commentId)
            .orElseThrow(() -> new ApiException(ErrorCode.RESOURCE_NOT_FOUND));
    User currentUser = userService.getCurrentUser();
    // Check if current user is the owner of the comment
    if (!comment.getUserId().equals(currentUser.getId())) {
      throw new ApiException(ErrorCode.ACCESS_DENIED);
    }
    comment.setText(request.text());
    // Update mentioned users
    List<String> mentionedUserIds = extractMentionedUserIds(request.text());
    comment.setMentionedUserIds(mentionedUserIds);
    Comment updatedComment = commentStore.save(comment);
    log.info("Comment {} updated successfully by user {}", commentId, currentUser.getId());
    return convertToCommentResponse(updatedComment, currentUser.getId());
  }

  public void deleteComment(String commentId) {
    Comment comment =
        commentStore
            .findById(commentId)
            .orElseThrow(() -> new ApiException(ErrorCode.RESOURCE_NOT_FOUND));
    User currentUser = userService.getCurrentUser();
    // Check if current user is the owner of the comment
    if (!comment.getUserId().equals(currentUser.getId())) {
      throw new ApiException(ErrorCode.ACCESS_DENIED);
    }
    // Delete all replies if this is a parent comment
    if (comment.getParentCommentId() == null) {
      commentStore.deleteAllReplies(commentId);
    }
    // Delete comment likes
    commentLikeStore.deleteAllByCommentId(commentId);
    commentStore.delete(comment);
    // Update counts
    if (comment.getParentCommentId() != null) {
      // This is a reply, decrement parent's reply count
      commentStore.incrementReplyCount(comment.getParentCommentId(), -1);
    } else {
      // This is a top-level comment, decrement photo's comment count
      photoStore.incrementCommentCount(comment.getPhotoId(), -1);
    }
    log.info("Comment {} deleted successfully by user {}", commentId, currentUser.getId());
  }

  public List<CommentResponse> getPhotoComments(String photoId) {
    // Validate photo exists
    photoStore.findById(photoId).orElseThrow(() -> new ApiException(ErrorCode.RESOURCE_NOT_FOUND));
    User currentUser = null;
    try {
      currentUser = userService.getCurrentUser();
    } catch (ApiException e) {
    }
    // User not authenticated, continue without user context
    String currentUserId = currentUser != null ? currentUser.getId() : null;
    // Get only top-level comments (no parent)
    List<Comment> topLevelComments = commentStore.findTopLevelByPhotoId(photoId);
    return
    // Load all nested replies recursively
    topLevelComments.stream()
        .map(
            comment -> {
              CommentResponse response = convertToCommentResponse(comment, currentUserId);
              loadNestedReplies(response, currentUserId);
              return response;
            })
        .toList();
  }

  // Recursive method to load all nested replies
  private void loadNestedReplies(CommentResponse parentResponse, String currentUserId) {
    List<Comment> replies = commentStore.findReplies(parentResponse.getId());
    List<CommentResponse> replyResponses =
        // Recursively load nested replies
        replies.stream()
            .map(
                reply -> {
                  CommentResponse replyResponse = convertToCommentResponse(reply, currentUserId);
                  loadNestedReplies(replyResponse, currentUserId);
                  return replyResponse;
                })
            .toList();
    parentResponse.setReplies(replyResponses);
  }

  public List<CommentResponse> getCommentReplies(String commentId, int page, int size) {
    Comment parentComment =
        commentStore
            .findById(commentId)
            .orElseThrow(() -> new ApiException(ErrorCode.RESOURCE_NOT_FOUND));
    User currentUser = null;
    try {
      currentUser = userService.getCurrentUser();
    } catch (ApiException e) {
    }
    // User not authenticated
    String currentUserId = currentUser != null ? currentUser.getId() : null;
    List<Comment> replies = commentStore.findReplies(commentId, page, size);
    return replies.stream().map(reply -> convertToCommentResponse(reply, currentUserId)).toList();
  }

  public long getPhotoCommentsCount(String photoId) {
    return commentStore.countByPhotoId(photoId);
  }

  public CommentResponse getComment(String commentId) {
    Comment comment =
        commentStore
            .findById(commentId)
            .orElseThrow(() -> new ApiException(ErrorCode.RESOURCE_NOT_FOUND));
    User currentUser = null;
    try {
      currentUser = userService.getCurrentUser();
    } catch (ApiException e) {
    }
    // User not authenticated
    String currentUserId = currentUser != null ? currentUser.getId() : null;
    return convertToCommentResponse(comment, currentUserId);
  }

  public CommentResponse likeComment(String commentId) {
    Comment comment =
        commentStore
            .findById(commentId)
            .orElseThrow(() -> new ApiException(ErrorCode.RESOURCE_NOT_FOUND));
    User currentUser = userService.getCurrentUser();
    if (!commentLikeStore.exists(commentId, currentUser.getId())) {
      CommentLike like = new CommentLike();
      like.setCommentId(commentId);
      like.setUserId(currentUser.getId());
      like.setCreatedAt(Instant.now());
      commentLikeStore.save(like);
      commentStore.incrementLikeCount(commentId, 1);
      comment.setLikeCount(comment.getLikeCount() + 1);
      // Send notification
      notificationService.sendLikeCommentNotification(
          comment.getUserId(), currentUser, comment.getPhotoId(), commentId);
      log.info("User {} liked comment {}", currentUser.getId(), commentId);
    }
    return convertToCommentResponse(comment, currentUser.getId());
  }

  public void unlikeComment(String commentId) {
    Comment comment =
        commentStore
            .findById(commentId)
            .orElseThrow(() -> new ApiException(ErrorCode.RESOURCE_NOT_FOUND));
    User currentUser = userService.getCurrentUser();
    commentLikeStore
        .find(commentId, currentUser.getId())
        .ifPresent(
            like -> {
              commentLikeStore.delete(like);
              commentStore.incrementLikeCount(commentId, -1);
            });
  }

  // Helper methods
  private List<String> extractMentionedUserIds(String text) {
    List<String> mentionedUserIds = new ArrayList<>();
    Matcher matcher = MENTION_PATTERN.matcher(text);
    Set<String> usernames =
        matcher.results().map(result -> result.group(1)).collect(Collectors.toSet());
    for (String username : usernames) {
      userStore.findByUsername(username).ifPresent(user -> mentionedUserIds.add(user.getId()));
    }
    return mentionedUserIds;
  }

  private CommentResponse convertToCommentResponse(Comment comment, String currentUserId) {
    CommentResponse response = new CommentResponse();
    response.setId(comment.getId());
    response.setPhotoId(comment.getPhotoId());
    response.setUserId(comment.getUserId());
    response.setText(comment.getText());
    response.setCreatedAt(comment.getCreatedAt());
    response.setParentCommentId(comment.getParentCommentId());
    response.setLikeCount(comment.getLikeCount());
    response.setReplyCount(comment.getReplyCount());
    if (comment.getUser() != null) {
      response.setUsername(comment.getUser().getUsername());
      response.setUserImageUrl(userAvatarCacheService.getAvatar(comment.getUser().getUserId()));
    }
    // Check if current user liked this comment
    if (currentUserId != null) {
      response.setLikedByCurrentUser(commentLikeStore.exists(comment.getId(), currentUserId));
    }
    // Convert mentioned user IDs to response format
    if (comment.getMentionedUserIds() != null && !comment.getMentionedUserIds().isEmpty()) {
      List<CommentResponse.MentionedUser> mentionedUsers = new ArrayList<>();
      for (String userId : comment.getMentionedUserIds()) {
        userStore
            .findById(userId)
            .ifPresent(
                user -> {
                  CommentResponse.MentionedUser mu = new CommentResponse.MentionedUser();
                  mu.setUserId(user.getId());
                  mu.setUsername(user.getUsername());
                  mentionedUsers.add(mu);
                });
      }
      response.setMentionedUsers(mentionedUsers);
    }
    return response;
  }

  private List<CommentResponse> convertToCommentResponses(List<Comment> comments) {
    User currentUser = null;
    try {
      currentUser = userService.getCurrentUser();
    } catch (ApiException e) {
    }
    // User not authenticated
    String currentUserId = currentUser != null ? currentUser.getId() : null;
    return comments.stream()
        .map(comment -> convertToCommentResponse(comment, currentUserId))
        .toList();
  }

  public CommentService(
      final CommentStore commentStore,
      final CommentLikeStore commentLikeStore,
      final PhotoStore photoStore,
      final UserStore userStore,
      final UserProfileService userService,
      final NotificationService notificationService,
      final AvatarCache userAvatarCacheService,
      final GraphProjection neo4jGraphService) {
    this.commentStore = commentStore;
    this.commentLikeStore = commentLikeStore;
    this.photoStore = photoStore;
    this.userStore = userStore;
    this.userService = userService;
    this.notificationService = notificationService;
    this.userAvatarCacheService = userAvatarCacheService;
    this.neo4jGraphService = neo4jGraphService;
  }
}
