import { useState, useEffect } from 'react';
import { Heart, MessageCircle, CornerDownRight } from 'lucide-react';
import { createComment, toggleCommentLike } from '../../services/commentService';
import { showToast } from '../../utils/toastService';
import { useAuthContext } from '../../context/AuthContext';
import { DEFAULT_AVATAR } from '../../utils/constants';
import '../../assets/styles/components/commentSection.css';

const CommentItem = ({ 
  comment, 
  onReply, 
  onLike, 
  formatDate, 
  currentUserId,
  level = 0 
}) => {
  const [showReplies, setShowReplies] = useState(level <= 1 && comment.replies?.length > 0);
  const [isLiking, setIsLiking] = useState(false);
  const [localLikeCount, setLocalLikeCount] = useState(comment.likeCount || 0);
  const [localIsLiked, setLocalIsLiked] = useState(comment.isLikedByCurrentUser || false);

  const handleLike = async () => {
    if (isLiking) return;
    setIsLiking(true);
    
    // Optimistic update
    const wasLiked = localIsLiked;
    setLocalIsLiked(!localIsLiked);
    setLocalLikeCount(prev => wasLiked ? prev - 1 : prev + 1);
    
    try {
      await onLike(comment.id);
    } catch (error) {
      // Revert on error
      setLocalIsLiked(wasLiked);
      setLocalLikeCount(prev => wasLiked ? prev + 1 : prev - 1);
    } finally {
      setIsLiking(false);
    }
  };

  // Level >= 1 đều có cùng indent (level 2+ không thụt thêm nữa)
  const isReply = level >= 1;

  return (
    <div className={`comment-item ${isReply ? 'reply-item' : ''}`}>
      <img
        src={comment.userImageUrl || DEFAULT_AVATAR}
        alt={comment.username}
        className="comment-avatar"
        onError={e => { e.currentTarget.src = DEFAULT_AVATAR; }}
      />
      <div className="comment-body">
        <div className="comment-header">
          <span className="comment-username">{comment.username}</span>
          <span className="comment-text">{comment.text}</span>
        </div>
        <div className="comment-actions">
          <span className="comment-time">{formatDate(comment.createdAt)}</span>
          {localLikeCount > 0 && (
            <span className="comment-likes">{localLikeCount} lượt thích</span>
          )}
          <button 
            className="comment-action-btn"
            onClick={() => onReply(comment)}
          >
            Trả lời
          </button>
        </div>
        
        {/* Replies toggle - hiển thị cho level 0 và level 1 */}
        {level <= 1 && comment.replyCount > 0 && (
          <button 
            className="view-replies-btn"
            onClick={() => setShowReplies(!showReplies)}
          >
            <CornerDownRight size={14} />
            {showReplies ? 'Ẩn câu trả lời' : `Xem ${comment.replyCount} câu trả lời`}
          </button>
        )}
        
        {/* Nested replies */}
        {showReplies && comment.replies && comment.replies.length > 0 && (
          <div className="replies-container">
            {comment.replies.map((reply) => (
              <CommentItem
                key={reply.id}
                comment={reply}
                onReply={onReply}
                onLike={onLike}
                formatDate={formatDate}
                currentUserId={currentUserId}
                level={level + 1}
              />
            ))}
          </div>
        )}
      </div>
      
      {/* Like button for comment */}
      <button 
        className={`comment-like-btn ${localIsLiked ? 'liked' : ''}`}
        onClick={handleLike}
        disabled={isLiking}
      >
        <Heart 
          size={14} 
          fill={localIsLiked ? 'red' : 'none'} 
          stroke={localIsLiked ? 'red' : 'currentColor'} 
        />
      </button>
    </div>
  );
};

const CommentSection = ({ 
  photoId, 
  comments: initialComments, 
  onCommentAdded,
  formatDate,
  currentUserId,
  currentUserImageUrl,
  hideForm = false,
  onReplyClick
}) => {
  const { user } = useAuthContext();
  const [comments, setComments] = useState(initialComments || []);
  const [newComment, setNewComment] = useState('');
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [replyingTo, setReplyingTo] = useState(null);

  // Update comments when props change
  useEffect(() => {
    setComments(initialComments || []);
  }, [initialComments]);

  const handleReply = (comment) => {
    setReplyingTo(comment);
    setNewComment(`@${comment.username} `);
    // Focus the input
    const input = document.getElementById('comment-input-section');
    if (input) input.focus();
    // Notify parent if callback provided
    if (onReplyClick) {
      onReplyClick(comment);
    }
  };

  const handleLike = async (commentId) => {
    await toggleCommentLike(commentId);
  };

  // Recursive helper to add reply to nested comments
  const addReplyToComments = (comments, parentId, newReply) => {
    return comments.map(comment => {
      // Check if this is the direct parent
      if (comment.id === parentId) {
        return {
          ...comment,
          replyCount: (comment.replyCount || 0) + 1,
          replies: [...(comment.replies || []), newReply]
        };
      }
      // Check nested replies recursively
      if (comment.replies && comment.replies.length > 0) {
        const updatedReplies = addReplyToComments(comment.replies, parentId, newReply);
        // Check if reply was added to nested level
        const replyAdded = updatedReplies !== comment.replies;
        if (replyAdded) {
          return {
            ...comment,
            replyCount: (comment.replyCount || 0) + 1,
            replies: updatedReplies
          };
        }
      }
      return comment;
    });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!newComment.trim() || isSubmitting) return;

    setIsSubmitting(true);
    try {
      const commentData = {
        content: newComment.trim(),
        parentCommentId: replyingTo?.id || null,
      };

      // Extract mentioned usernames from comment text
      const mentionMatches = newComment.match(/@(\w+)/g);
      if (mentionMatches) {
        commentData.mentionedUsernames = mentionMatches.map(m => m.substring(1));
      }

      const response = await createComment(photoId, commentData);
      const apiCommentData = response.data ?? response;

      // Augment with current user info if missing (for immediate display)
      const newCommentData = {
        ...apiCommentData,
        userImageUrl: apiCommentData.userImageUrl || currentUserImageUrl,
        username: apiCommentData.username || user?.username,
        replies: [], // Initialize with empty replies array
      };

      if (replyingTo) {
        // Add reply to parent comment (supports all nesting levels)
        setComments(prevComments =>
          addReplyToComments(prevComments, replyingTo.id, newCommentData)
        );
      } else {
        // Add as top-level comment
        setComments(prev => [...prev, newCommentData]);
      }

      setNewComment('');
      setReplyingTo(null);

      if (onCommentAdded) {
        onCommentAdded(newCommentData);
      }

      showToast('success', 'Đã thêm bình luận');
    } catch (error) {
      showToast('error', 'Không thể thêm bình luận');
    } finally {
      setIsSubmitting(false);
    }
  };

  const cancelReply = () => {
    setReplyingTo(null);
    setNewComment('');
  };

  return (
    <div className="comment-section">
      {/* Comments list */}
      <div className="comments-list">
        {comments.length > 0 ? (
          comments.map((comment) => (
            <CommentItem
              key={comment.id}
              comment={comment}
              onReply={handleReply}
              onLike={handleLike}
              formatDate={formatDate}
              currentUserId={currentUserId}
            />
          ))
        ) : (
          <p className="no-comments">Chưa có bình luận nào. Hãy là người đầu tiên!</p>
        )}
      </div>

      {/* Only show form if not hidden */}
      {!hideForm && (
        <>
          {/* Reply indicator */}
          {replyingTo && (
            <div className="replying-to">
              <span>Đang trả lời <strong>@{replyingTo.username}</strong></span>
              <button onClick={cancelReply} className="cancel-reply-btn">✕</button>
            </div>
          )}

          {/* Comment input */}
          <form onSubmit={handleSubmit} className="comment-form">
            <input
              id="comment-input-section"
              type="text"
              placeholder={replyingTo ? `Trả lời @${replyingTo.username}...` : 'Thêm bình luận...'}
              value={newComment}
              onChange={(e) => setNewComment(e.target.value)}
              disabled={isSubmitting}
              className="comment-input"
              autoComplete="off"
            />
            <button 
              type="submit" 
              disabled={!newComment.trim() || isSubmitting}
              className="comment-submit-btn"
            >
              {isSubmitting ? 'Đang gửi...' : 'Đăng'}
            </button>
          </form>
        </>
      )}
    </div>
  );
};

export default CommentSection;
