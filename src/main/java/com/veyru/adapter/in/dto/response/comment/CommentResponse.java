package com.veyru.adapter.in.dto.response.comment;

import com.veyru.application.result.comment.CommentResult;
import java.time.Instant;
import java.util.List;

public record CommentResponse(
    String id,
    String photoId,
    String userId,
    String username,
    String userImageUrl,
    String text,
    Instant createdAt,
    String parentCommentId,
    long likeCount,
    long replyCount,
    boolean isLikedByCurrentUser,
    List<CommentResponse> replies,
    List<MentionedUser> mentionedUsers) {
  public record MentionedUser(String userId, String username) {
    static MentionedUser from(CommentResult.MentionedUser value) {
      return new MentionedUser(value.getUserId(), value.getUsername());
    }
  }

  public static CommentResponse from(CommentResult value) {
    return new CommentResponse(
        value.getId(), value.getPhotoId(), value.getUserId(), value.getUsername(),
        value.getUserImageUrl(), value.getText(), value.getCreatedAt(), value.getParentCommentId(),
        value.getLikeCount(), value.getReplyCount(), value.isLikedByCurrentUser(),
        value.getReplies().stream().map(CommentResponse::from).toList(),
        value.getMentionedUsers().stream().map(MentionedUser::from).toList());
  }
}
