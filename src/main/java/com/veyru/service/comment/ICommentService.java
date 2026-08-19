package com.veyru.service.comment;

import com.veyru.dto.request.comment.CreateCommentRequest;
import com.veyru.dto.request.comment.UpdateCommentRequest;
import com.veyru.dto.response.comment.CommentResponse;
import java.util.List;

public interface ICommentService {
  CommentResponse createComment(String photoId, CreateCommentRequest request);

  CommentResponse updateComment(String commentId, UpdateCommentRequest request);

  void deleteComment(String commentId);

  List<CommentResponse> getPhotoComments(String photoId);

  List<CommentResponse> getCommentReplies(String commentId, int page, int size);

  long getPhotoCommentsCount(String photoId);

  CommentResponse getComment(String commentId);

  CommentResponse toggleCommentLike(String commentId);
}
