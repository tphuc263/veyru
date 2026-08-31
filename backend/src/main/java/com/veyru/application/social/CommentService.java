package com.veyru.application.social;

import com.veyru.application.common.error.UseCaseError;
import com.veyru.application.common.error.UseCaseException;
import com.veyru.application.identity.UserProfileService;
import com.veyru.application.notification.NotificationService;
import com.veyru.application.port.out.AvatarCache;
import com.veyru.application.port.out.CommentLikeStore;
import com.veyru.application.port.out.CommentStore;
import com.veyru.application.port.out.GraphProjection;
import com.veyru.application.port.out.PhotoStore;
import com.veyru.application.port.out.UserStore;
import com.veyru.application.result.comment.CommentResult;
import com.veyru.domain.model.Comment;
import com.veyru.domain.model.CommentLike;
import com.veyru.domain.model.Photo;
import com.veyru.domain.model.User;
import java.time.Clock;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class CommentService {
  private static final Pattern MENTION_PATTERN = Pattern.compile("@(\\w+)");
  private final CommentStore commentStore;
  private final CommentLikeStore commentLikeStore;
  private final PhotoStore photoStore;
  private final UserStore userStore;
  private final UserProfileService userService;
  private final NotificationService notificationService;
  private final AvatarCache userAvatarCacheService;
  private final GraphProjection neo4jGraphService;
  private final Clock clock;

  public CommentResult createComment(String photoId, CreateCommentCommand request) {
    Photo photo =
        photoStore
            .findById(photoId)
            .orElseThrow(() -> new UseCaseException(UseCaseError.RESOURCE_NOT_FOUND));
    User currentUser = userService.requireCurrentUser();
    String parentCommentId = request.parentCommentId();
    Comment parentComment = null;
    if (parentCommentId != null && !parentCommentId.isEmpty()) {
      parentComment =
          commentStore
              .findById(parentCommentId)
              .orElseThrow(() -> new UseCaseException(UseCaseError.RESOURCE_NOT_FOUND));
    }
    List<String> mentionedUserIds = extractMentionedUserIds(request.text());
    Comment comment =
        Comment.create(
            photoId,
            currentUser.id(),
            currentUser.username(),
            request.text(),
            mentionedUserIds,
            clock.instant());
    if (parentComment != null) comment = comment.asReplyTo(parentComment);
    Comment savedComment = commentStore.save(comment);
    if (parentComment != null) {
      commentStore.incrementReplyCount(parentCommentId, 1);
      notificationService.sendReplyCommentNotification(
          parentComment.userId(), currentUser, photoId, savedComment.id(), photo.imageUrl());
    } else {
      photoStore.incrementCommentCount(photoId, 1);
    }
    if (parentComment == null) {
      notificationService.sendCommentPhotoNotification(
          photo.author().userId(), currentUser, photoId, savedComment.id(), photo.imageUrl());
    }
    for (String mentionedUserId : mentionedUserIds) {
      notificationService.sendMentionNotification(
          mentionedUserId, currentUser, photoId, savedComment.id(), photo.imageUrl());
    }
    neo4jGraphService.createCommentRelationship(currentUser.id(), photoId);
    return convertToCommentResponse(savedComment, currentUser.id());
  }

  public CommentResult updateComment(String commentId, UpdateCommentCommand request) {
    Comment comment =
        commentStore
            .findById(commentId)
            .orElseThrow(() -> new UseCaseException(UseCaseError.RESOURCE_NOT_FOUND));
    User currentUser = userService.requireCurrentUser();
    if (!comment.userId().equals(currentUser.id())) {
      throw new UseCaseException(UseCaseError.ACCESS_DENIED);
    }
    List<String> mentionedUserIds = extractMentionedUserIds(request.text());
    Comment updatedComment =
        commentStore.save(comment.editedBy(currentUser.id(), request.text(), mentionedUserIds));
    return convertToCommentResponse(updatedComment, currentUser.id());
  }

  public void deleteComment(String commentId) {
    Comment comment =
        commentStore
            .findById(commentId)
            .orElseThrow(() -> new UseCaseException(UseCaseError.RESOURCE_NOT_FOUND));
    User currentUser = userService.requireCurrentUser();
    if (!comment.userId().equals(currentUser.id())) {
      throw new UseCaseException(UseCaseError.ACCESS_DENIED);
    }
    if (comment.parentCommentId() == null) {
      commentStore.deleteAllReplies(commentId);
    }
    commentLikeStore.deleteAllByCommentId(commentId);
    commentStore.delete(comment);
    if (comment.parentCommentId() != null) {
      commentStore.incrementReplyCount(comment.parentCommentId(), -1);
    } else {
      photoStore.incrementCommentCount(comment.photoId(), -1);
    }
  }

  public List<CommentResult> getPhotoComments(String photoId) {
    photoStore
        .findById(photoId)
        .orElseThrow(() -> new UseCaseException(UseCaseError.RESOURCE_NOT_FOUND));
    String currentUserId = userService.findCurrentUser().map(User::id).orElse(null);
    List<Comment> topLevelComments = commentStore.findTopLevelByPhotoId(photoId);
    return topLevelComments.stream()
        .map(
            comment -> {
              CommentResult response = convertToCommentResponse(comment, currentUserId);
              loadNestedReplies(response, currentUserId);
              return response;
            })
        .toList();
  }

  private void loadNestedReplies(CommentResult parentResponse, String currentUserId) {
    List<Comment> replies = commentStore.findReplies(parentResponse.getId());
    List<CommentResult> replyResponses =
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
    commentStore
        .findById(commentId)
        .orElseThrow(() -> new UseCaseException(UseCaseError.RESOURCE_NOT_FOUND));
    String currentUserId = userService.findCurrentUser().map(User::id).orElse(null);
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
    String currentUserId = userService.findCurrentUser().map(User::id).orElse(null);
    return convertToCommentResponse(comment, currentUserId);
  }

  public CommentResult likeComment(String commentId) {
    Comment comment =
        commentStore
            .findById(commentId)
            .orElseThrow(() -> new UseCaseException(UseCaseError.RESOURCE_NOT_FOUND));
    User currentUser = userService.requireCurrentUser();
    if (!commentLikeStore.exists(commentId, currentUser.id())) {
      commentLikeStore.save(CommentLike.create(commentId, currentUser.id(), clock.instant()));
      commentStore.incrementLikeCount(commentId, 1);
      comment = comment.withRecordedLike();
      notificationService.sendLikeCommentNotification(
          comment.userId(), currentUser, comment.photoId(), commentId);
    }
    return convertToCommentResponse(comment, currentUser.id());
  }

  public void unlikeComment(String commentId) {
    Comment comment =
        commentStore
            .findById(commentId)
            .orElseThrow(() -> new UseCaseException(UseCaseError.RESOURCE_NOT_FOUND));
    User currentUser = userService.requireCurrentUser();
    commentLikeStore
        .find(commentId, currentUser.id())
        .ifPresent(
            like -> {
              commentLikeStore.delete(like);
              commentStore.incrementLikeCount(commentId, -1);
            });
  }

  private List<String> extractMentionedUserIds(String text) {
    List<String> mentionedUserIds = new ArrayList<>();
    Matcher matcher = MENTION_PATTERN.matcher(text);
    Set<String> usernames =
        matcher.results().map(result -> result.group(1)).collect(Collectors.toSet());
    for (String username : usernames) {
      userStore.findByUsername(username).ifPresent(user -> mentionedUserIds.add(user.id()));
    }
    return mentionedUserIds;
  }

  private CommentResult convertToCommentResponse(Comment comment, String currentUserId) {
    CommentResult response = new CommentResult();
    response.setId(comment.id());
    response.setPhotoId(comment.photoId());
    response.setUserId(comment.userId());
    response.setText(comment.text());
    response.setCreatedAt(comment.createdAt());
    response.setParentCommentId(comment.parentCommentId());
    response.setLikeCount(comment.likeCount());
    response.setReplyCount(comment.replyCount());
    response.setUsername(comment.author().username());
    response.setUserImageUrl(userAvatarCacheService.getAvatar(comment.author().userId()));
    if (currentUserId != null) {
      response.setLikedByCurrentUser(commentLikeStore.exists(comment.id(), currentUserId));
    }
    if (!comment.mentionedUserIds().isEmpty()) {
      List<CommentResult.MentionedUser> mentionedUsers = new ArrayList<>();
      for (String userId : comment.mentionedUserIds()) {
        userStore
            .findById(userId)
            .ifPresent(
                user -> {
                  CommentResult.MentionedUser mu = new CommentResult.MentionedUser();
                  mu.setUserId(user.id());
                  mu.setUsername(user.username());
                  mentionedUsers.add(mu);
                });
      }
      response.setMentionedUsers(mentionedUsers);
    }
    return response;
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
