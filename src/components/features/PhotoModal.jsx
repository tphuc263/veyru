import { useEffect, useState, useRef } from 'react';
import { Link } from 'react-router-dom';
import '../../assets/styles/components/photoModal.css';
import { getPhotoById } from '../../services/photoService';
import { getPhotoComments, createComment } from '../../services/commentService';
import { toggleFavorite } from '../../services/favoriteService';
import { follow, unfollow } from '../../services/followService';
import { getRelatedPhotos } from '../../services/recommendationService';
import { showToast } from '../../utils/toastService';
import { DEFAULT_AVATAR } from '../../utils/constants';
import { Loader } from '../common/Loader';
import { useOptimisticLike } from '../../hooks/useOptimisticLike';
import { Heart, MessageCircle } from 'lucide-react';
import ShareModal from './ShareModal';
import CommentSection from './CommentSection';
import { useAuth } from '../../hooks/useAuth';

const PhotoModal = ({ photoId, onClose, onPhotoUpdate, onPhotoClick }) => {
  const [photoDetail, setPhotoDetail] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [comments, setComments] = useState([]);
  const [isSaved, setIsSaved] = useState(false);
  const [isSaving, setIsSaving] = useState(false);
  const [relatedPhotos, setRelatedPhotos] = useState([]);
  const [relatedLoading, setRelatedLoading] = useState(false);
  const [showShareModal, setShowShareModal] = useState(false);
  const [localShareCount, setLocalShareCount] = useState(0);
  const [localCommentCount, setLocalCommentCount] = useState(0);
  const [newComment, setNewComment] = useState('');
  const [isSubmittingComment, setIsSubmittingComment] = useState(false);
  const [replyingTo, setReplyingTo] = useState(null);
  const [isFollowing, setIsFollowing] = useState(false);
  const [isFollowLoading, setIsFollowLoading] = useState(false);
  const commentInputRef = useRef(null);
  const { user } = useAuth();

  // Pass actual photoDetail values directly - the hook will sync when they change
  const { isLiked, likesCount, isProcessing, handleLike } = useOptimisticLike(
    photoId,
    photoDetail?.isLikedByCurrentUser ?? false,
    photoDetail?.likeCount ?? 0,
    onPhotoUpdate
  );

  useEffect(() => {
    const fetchPhotoDetail = async () => {
      try {
        setLoading(true);
        const response = await getPhotoById(photoId);
        setPhotoDetail(response);
        setIsSaved(response.isSavedByCurrentUser ?? false);
        setIsFollowing(response.followingByCurrentUser ?? false);
        setLocalShareCount(response.shareCount || 0);
        setLocalCommentCount(response.commentCount || 0);
        
        // Fetch comments with nested structure
        try {
          const commentsResponse = await getPhotoComments(photoId);
          setComments(commentsResponse.data ?? commentsResponse ?? []);
        } catch (err) {
          console.error('Failed to load comments:', err);
          setComments([]);
        }
      } catch (err) {
        setError(err.message);
      } finally {
        setLoading(false);
      }
    };

    if (photoId) {
      fetchPhotoDetail();
    }
  }, [photoId]);

  // Load related photos (AI recommendation)
  useEffect(() => {
    const fetchRelatedPhotos = async () => {
      if (!photoId) return;
      setRelatedLoading(true);
      try {
        const photos = await getRelatedPhotos(photoId, 6);
        setRelatedPhotos(photos);
      } catch (err) {
        console.error('Failed to load related photos:', err);
      } finally {
        setRelatedLoading(false);
      }
    };
    fetchRelatedPhotos();
  }, [photoId]);

  useEffect(() => {
    document.body.style.overflow = 'hidden';
    return () => {
      document.body.style.overflow = 'unset';
    };
  }, []);

  const handleBackdropClick = (e) => {
    if (e.target.classList.contains('photo-modal-backdrop')) {
      onClose();
    }
  };

  const formatDate = (dateString) => {
    const date = new Date(dateString);
    const now = new Date();
    const diffMs = now - date;
    const diffMins = Math.floor(diffMs / 60000);
    const diffHours = Math.floor(diffMs / 3600000);
    const diffDays = Math.floor(diffMs / 86400000);

    if (diffMins < 1) return 'vừa xong';
    if (diffMins < 60) return `${diffMins} phút trước`;
    if (diffHours < 24) return `${diffHours} giờ trước`;
    if (diffDays < 7) return `${diffDays} ngày trước`;
    
    return date.toLocaleDateString('vi-VN', { 
      day: 'numeric', 
      month: 'long', 
      year: 'numeric' 
    });
  };

  const handleCommentAdded = (newCommentData) => {
    setLocalCommentCount(prev => prev + 1);
    if (onPhotoUpdate && photoDetail) {
      const updatedPhoto = {
        ...photoDetail,
        commentCount: localCommentCount + 1,
        isLikedByCurrentUser: isLiked,
        likeCount: likesCount
      };
      onPhotoUpdate(photoId, updatedPhoto);
    }
  };

  const handleReplyClick = (comment) => {
    setReplyingTo(comment);
    setNewComment(`@${comment.username} `);
    commentInputRef.current?.focus();
  };

  // Recursive helper to add reply to nested comments (same as CommentSection)
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

  const handleCommentSubmit = async (e) => {
    e.preventDefault();
    if (!newComment.trim() || isSubmittingComment) return;

    setIsSubmittingComment(true);
    try {
      const commentData = {
        content: newComment.trim(),
        parentCommentId: replyingTo?.id || null,
      };

      const response = await createComment(photoId, commentData);
      const apiCommentData = response.data ?? response;

      const newCommentData = {
        ...apiCommentData,
        userImageUrl: apiCommentData.userImageUrl || user?.avatarUrl,
        username: apiCommentData.username || user?.username,
        replies: [],
      };

      if (replyingTo) {
        setComments(prevComments =>
          addReplyToComments(prevComments, replyingTo.id, newCommentData)
        );
      } else {
        setComments(prev => [...prev, newCommentData]);
      }

      setNewComment('');
      setReplyingTo(null);
      handleCommentAdded(newCommentData);
      showToast('success', 'Đã thêm bình luận');
    } catch (error) {
      showToast('error', 'Không thể thêm bình luận');
    } finally {
      setIsSubmittingComment(false);
      // Restore focus after re-render completes so cursor stays in input
      setTimeout(() => commentInputRef.current?.focus(), 0);
    }
  };

  const cancelReply = () => {
    setReplyingTo(null);
    setNewComment('');
  };

  const handleSave = async () => {
    if (isSaving) return;
    setIsSaving(true);
    const prevSaved = isSaved;
    setIsSaved(!isSaved);
    try {
      await toggleFavorite(photoId);
    } catch (err) {
      setIsSaved(prevSaved);
      console.error('Failed to toggle save:', err);
    } finally {
      setIsSaving(false);
    }
  };

  if (loading) {
    return (
      <div className="photo-modal-backdrop" onClick={handleBackdropClick}>
        <div className="photo-modal-content">
          <Loader />
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="photo-modal-backdrop" onClick={handleBackdropClick}>
        <div className="photo-modal-content">
          <p className="error-message">Lỗi: {error}</p>
          <button onClick={onClose} className="close-button">Đóng</button>
        </div>
      </div>
    );
  }

  if (!photoDetail) {
    return null;
  }

  return (
    <div className="photo-modal-backdrop" onClick={handleBackdropClick}>
      <div className="photo-modal-content">
        <button className="modal-close-button" onClick={onClose}>
          ✕
        </button>
        
        <div className="photo-modal-container">
          {/* Left side - Photo */}
          <div className="photo-modal-image-section">
            <img src={photoDetail.imageUrl} alt={photoDetail.caption} />
          </div>

          {/* Right side - Details */}
          <div className="photo-modal-details-section">
            {/* Header */}
            <div className="photo-modal-header">
              <img 
                src={photoDetail.userImageUrl || DEFAULT_AVATAR} 
                alt={photoDetail.username} 
                className="user-avatar"
                onError={e => { e.currentTarget.src = DEFAULT_AVATAR; }}
              />
              <Link
                to={`/profile/${photoDetail.userId}`}
                className="modal-username-link"
                onClick={onClose}
              >
                {photoDetail.username}
              </Link>
              {user && photoDetail.userId && String(user.id) !== String(photoDetail.userId) && (
                <button
                  className={`modal-follow-btn ${isFollowing ? 'following' : ''}`}
                  disabled={isFollowLoading}
                  onClick={async () => {
                    setIsFollowLoading(true);
                    try {
                      if (isFollowing) {
                        await unfollow(photoDetail.userId);
                        setIsFollowing(false);
                      } else {
                        await follow(photoDetail.userId);
                        setIsFollowing(true);
                      }
                    } catch (err) {
                      console.error('Follow toggle failed:', err);
                    } finally {
                      setIsFollowLoading(false);
                    }
                  }}
                >
                  {isFollowLoading ? '...' : isFollowing ? 'Following' : 'Follow'}
                </button>
              )}
            </div>

            {/* Caption and Comments */}
            <div className="photo-modal-content-area">
              {/* Caption */}
              {photoDetail.caption && (
                <div className="photo-modal-caption">
                  <img 
                    src={photoDetail.userImageUrl || DEFAULT_AVATAR} 
                    alt={photoDetail.username} 
                    className="user-avatar-small"
                    onError={e => { e.currentTarget.src = DEFAULT_AVATAR; }}
                  />
                  <div className="caption-content">
                    <Link to={`/profile/${photoDetail.userId}`} className="modal-username-link" onClick={onClose}>{photoDetail.username}</Link>
                    <p className="caption-text">{photoDetail.caption}</p>
                    <span className="timestamp">{formatDate(photoDetail.createdAt)}</span>
                  </div>
                </div>
              )}

              {/* Comments Section - list only, form is in footer */}
              <CommentSection
                photoId={photoId}
                comments={comments}
                onCommentAdded={handleCommentAdded}
                formatDate={formatDate}
                currentUserId={user?.id}
                hideForm={true}
                onReplyClick={handleReplyClick}
              />
            </div>

            {/* Footer - Actions */}
            <div className="photo-modal-footer">
              <div className="actions-row">
                <div className="action-buttons">
                  <button 
                    className={`action-btn ${isLiked ? 'liked' : ''}`}
                    onClick={handleLike}
                    disabled={isProcessing}
                    aria-label={isLiked ? 'Unlike' : 'Like'}
                  >
                    <svg width="24" height="24" viewBox="0 0 24 24" fill={isLiked ? "red" : "none"} stroke="currentColor" strokeWidth="2">
                      <path d="M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 0 0-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 0 0 0-7.78z"></path>
                    </svg>
                  </button>
                  <button className="action-btn">
                    <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                      <path d="M21 11.5a8.38 8.38 0 0 1-.9 3.8 8.5 8.5 0 0 1-7.6 4.7 8.38 8.38 0 0 1-3.8-.9L3 21l1.9-5.7a8.38 8.38 0 0 1-.9-3.8 8.5 8.5 0 0 1 4.7-7.6 8.38 8.38 0 0 1 3.8-.9h.5a8.48 8.48 0 0 1 8 8v.5z"></path>
                    </svg>
                  </button>
                  <button 
                    className="action-btn"
                    onClick={() => setShowShareModal(true)}
                    aria-label="Share"
                  >
                    <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                      <line x1="22" y1="2" x2="11" y2="13"></line>
                      <polygon points="22 2 15 22 11 13 2 9 22 2"></polygon>
                    </svg>
                  </button>
                </div>
                <button 
                  className={`action-btn save-btn ${isSaved ? 'saved' : ''}`}
                  onClick={handleSave}
                  disabled={isSaving}
                  aria-label={isSaved ? 'Unsave' : 'Save'}
                >
                  <svg width="24" height="24" viewBox="0 0 24 24" fill={isSaved ? "currentColor" : "none"} stroke="currentColor" strokeWidth="2">
                    <path d="M19 21l-7-5-7 5V5a2 2 0 0 1 2-2h10a2 2 0 0 1 2 2z"></path>
                  </svg>
                </button>
              </div>
              
              <div className="likes-count">
                <strong>{likesCount}</strong> lượt thích
                {localShareCount > 0 && (
                    <span className="posts-count-inline"> · <strong>{localShareCount}</strong> lượt chia sẻ</span>
                )}
              </div>
              
              <div className="post-date">
                {formatDate(photoDetail.createdAt)}
              </div>

              {/* Tags */}
              {photoDetail.tags && photoDetail.tags.length > 0 && (
                <div className="photo-tags">
                  {photoDetail.tags.map((tag, index) => (
                    <span key={index} className="tag">#{tag}</span>
                  ))}
                </div>
              )}

              {/* Reply indicator */}
              {replyingTo && (
                <div className="replying-to">
                  <span>Đang trả lời <strong>@{replyingTo.username}</strong></span>
                  <button onClick={cancelReply} className="cancel-reply-btn">✕</button>
                </div>
              )}

              {/* Comment input */}
              <form onSubmit={handleCommentSubmit} className="comment-form">
                <input
                  ref={commentInputRef}
                  type="text"
                  placeholder={replyingTo ? `Trả lời @${replyingTo.username}...` : 'Thêm bình luận...'}
                  value={newComment}
                  onChange={(e) => setNewComment(e.target.value)}
                  disabled={isSubmittingComment}
                  className="comment-input"
                  autoComplete="off"
                />
                <button 
                  type="submit" 
                  disabled={!newComment.trim() || isSubmittingComment}
                  className="comment-submit-btn"
                >
                  {isSubmittingComment ? 'Đang gửi...' : 'Đăng'}
                </button>
              </form>
            </div>
          </div>
        </div>

        {/* Related Photos Section */}
        {relatedPhotos.length > 0 && (
          <div className="related-photos-section">
            <div className="related-photos-header">
              <h4>More posts like this</h4>
            </div>
            <div className="related-photos-grid">
              {relatedPhotos.map(photo => (
                <div
                  key={photo.id}
                  className="related-photo-item"
                  onClick={() => {
                    onPhotoClick(photo.id);
                  }}
                >
                  <img src={photo.imageUrl} alt={photo.caption || 'Related photo'} loading="lazy" />
                  <div className="related-photo-overlay">
                    <div className="related-photo-stats">
                      <span><Heart size={16} fill="white" stroke="white" /> {photo.likeCount}</span>
                      <span><MessageCircle size={16} fill="white" stroke="white" /> {photo.commentCount}</span>
                    </div>
                  </div>
                </div>
              ))}
            </div>
          </div>
        )}
        {relatedLoading && (
          <div className="related-photos-loading">
            <Loader />
          </div>
        )}
      </div>

      {showShareModal && (
        <ShareModal
          photoId={photoId}
          photoSrc={photoDetail?.imageUrl}
          onClose={() => setShowShareModal(false)}
          onShareSuccess={(updatedPhoto) => {
            setLocalShareCount(prev => prev + 1);
            if (onPhotoUpdate && updatedPhoto) {
              onPhotoUpdate(photoId, updatedPhoto);
            }
          }}
        />
      )}
    </div>
  );
};

export default PhotoModal;
