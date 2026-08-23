package com.veyru.application.social;

import com.veyru.application.identity.UserProfileService;
import com.veyru.application.notification.NotificationService;
import com.veyru.application.port.out.AvatarCache;
import com.veyru.application.port.out.CommentLikeStore;
import com.veyru.application.port.out.CommentStore;
import com.veyru.application.port.out.GraphProjection;
import com.veyru.application.port.out.PhotoStore;
import com.veyru.application.port.out.UserStore;
import com.veyru.application.result.comment.CommentResult;
import com.veyru.application.common.error.UseCaseException;
import com.veyru.application.common.error.UseCaseError;
import com.veyru.domain.model.Comment;
import com.veyru.domain.model.CommentLike;
import com.veyru.domain.model.Photo;
import com.veyru.domain.model.User;
import java.time.Instant;
import java.time.Clock;
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
  private final Clock clock;
  // Pattern to match @username mentions
  private static final Pattern MENTION_PATTERN = Pattern.compile("@(\\w+)");

  public CommentResult createComment(String photoId, CreateCommentCommand request) {
    // Validate photo exists
    Photo photo =
        photoStore
            .findById(photoId)
            .orElseThrow(() -> new UseCaseException(UseCaseError.RESOURCE_NOT_FOUND));
    User currentUser = userService.requireCurrentUser();
    // Handle nested comments (replies)
    String parentCommentId = request.parentCommentId();
    Comment parentComment = null;
    if (parentCommentId != null && !parentCommentId.isEmpty()) {
      parentComment =
          commentStore
              .findById(parentCommentId)
              .orElseThrow(() -> new UseCaseException(UseCaseError.RESOURCE_NOT_FOUND));
    }
    // Extract mentioned users from text
    List<String> mentionedUserIds = extractMentionedUserIds(request.text());
    Comment comment =
        Comment.create(
            photoId,
            currentUser.getId(),
            currentUser.getUsername(),
            request.text(),
            mentionedUserIds,
            clock.instant());
    if (parentComment != null) comment.replyTo(parentComment);
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

  public CommentResult updateComment(String commentId, UpdateCommentCommand request) {
    Comment comment =
        commentStore
            .findById(commentId)
            .orElseThrow(() -> new UseCaseException(UseCaseError.RESOURCE_NOT_FOUND));
    User currentUser = userService.requireCurrentUser();
    // Check if current user is the owner of the comment
    if (!comment.getUserId().equals(currentUser.getId())) {
      throw new UseCaseException(UseCaseError.ACCESS_DENIED);
    }
    List<String> mentionedUserIds = extractMentionedUserIds(request.text());
    comment.edit(currentUser.getId(), request.text(), mentionedUserIds);
    Comment updatedComment = commentStore.save(comment);
    log.info("Comment {} updated successfully by user {}", commentId, currentUser.getId());
    return convertToCommentResponse(updatedComment, currentUser.getId());
  }

  public void deleteComment(String commentId) {
    Comment comment =
        commentStore
            .findById(commentId)
            .orElseThrow(() -> new UseCaseException(UseCaseError.RESOURCE_NOT_FOUND));
    User currentUser = userService.requireCurrentUser();
    // Check if current user is the owner of the comment
    if (!comment.getUserId().equals(currentUser.getId())) {
      throw new UseCaseException(UseCaseError.ACCESS_DENIED);
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

  public List<CommentResult> getPhotoComments(String photoId) {
    // Validate photo exists
    photoStore.findById(photoId).orElseThrow(() -> new UseCaseException(UseCaseError.RESOURCE_NOT_FOUND));
    String currentUserId = userService.findCurrentUser().map(User::getId).orElse(null);
    // Get only top-level comments (no parent)
    List<Comment> topLevelComments = commentStore.findTopLevelByPhotoId(photoId);
    return
    // Load all nested replies recursively
    topLevelComments.stream()
        .map(
            comment -> {
              CommentResult response = convertToCommentResponse(comment, currentUserId);
              loadNestedReplies(response, currentUserId);
              return response;
            })
        .toList();
  }

  // Recursive method to load all nested replies
  private void loadNestedReplies(CommentResult parentResponse, String currentUserId) {
    List<Comment> replies = commentStore.findReplies(parentResponse.getId());
    List<CommentResult> replyResponses =
        // Recursively load nested replies
        replies.stream()
            .map(
                reply -> {
                  CommentResult replyResponse = convertToCommentResponse(reply, currentUserId);
                  loadNestedReplies(replyResponse, currentUserId);
                  return replyResponse;
                })
            .toList();
    parentResponse.setReplies(replyResponses);
  }

  public List<CommentResult> getCommentReplies(String commentId, int page, int size) {
    Comment parentComment =
        commentStore
            .findById(commentId)
            .orElseThrow(() -> new UseCaseException(UseCaseError.RESOURCE_NOT_FOUND));
    String currentUserId = userService.findCurrentUser().map(User::getId).orElse(null);
    List<Comment> replies = commentStore.findReplies(commentId, page, size);
    return replies.stream().map(reply -> convertToCommentResponse(reply, currentUserId)).toList();
  }

  public long getPhotoCommentsCount(String photoId) {
    return commentStore.countByPhotoId(photoId);
  }

  public CommentResult getComment(String commentId) {
    Comment comment =
        commentStore
            .findById(commentId)
            .orElseThrow(() -> new UseCaseException(UseCaseError.RESOURCE_NOT_FOUND));
    String currentUserId = userService.findCurrentUser().map(User::getId).orElse(null);
    return convertToCommentResponse(comment, currentUserId);
  }

  public CommentResult likeComment(String commentId) {
    Comment comment =
        commentStore
            .findById(commentId)
            .orElseThrow(() -> new UseCaseException(UseCaseError.RESOURCE_NOT_FOUND));
    User currentUser = userService.requireCurrentUser();
    if (!commentLikeStore.exists(commentId, currentUser.getId())) {
      CommentLike like = new CommentLike();
      like.setCommentId(commentId);
      like.setUserId(currentUser.getId());
      like.setCreatedAt(clock.instant());
      commentLikeStore.save(like);
      commentStore.incrementLikeCount(commentId, 1);
      comment.recordLike();
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
            .orElseThrow(() -> new UseCaseException(UseCaseError.RESOURCE_NOT_FOUND));
    User currentUser = userService.requireCurrentUser();
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

  private CommentResult convertToCommentResponse(Comment comment, String currentUserId) {
    CommentResult response = new CommentResult();
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
      List<CommentResult.MentionedUser> mentionedUsers = new ArrayList<>();
      for (String userId : comment.getMentionedUserIds()) {
        userStore
            .findById(userId)
            .ifPresent(
                user -> {
                  CommentResult.MentionedUser mu = new CommentResult.MentionedUser();
                  mu.setUserId(user.getId());
                  mu.setUsername(user.getUsername());
                  mentionedUsers.add(mu);
                });
      }
      response.setMentionedUsers(mentionedUsers);
    }
    return response;
  }

  private List<CommentResult> convertToCommentResponses(List<Comment> comments) {
    String currentUserId = userService.findCurrentUser().map(User::getId).orElse(null);
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
      final GraphProjection neo4jGraphService,
      final Clock clock) {
    this.commentStore = commentStore;
    this.commentLikeStore = commentLikeStore;
    this.photoStore = photoStore;
    this.userStore = userStore;
    this.userService = userService;
    this.notificationService = notificationService;
    this.userAvatarCacheService = userAvatarCacheService;
    this.neo4jGraphService = neo4jGraphService;
    this.clock = clock;
  }
}
