package share_app.tphucshareapp.service.comment;

import share_app.tphucshareapp.dto.request.comment.CreateCommentRequest;
import share_app.tphucshareapp.dto.request.comment.UpdateCommentRequest;
import share_app.tphucshareapp.dto.response.comment.CommentResponse;

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
