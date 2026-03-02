import { useEffect, useState, useRef } from 'react';
import '../../assets/styles/components/photoModal.css';
import { getPhotoById } from '../../services/photoService';
import { createComment } from '../../services/commentService';
import { toggleFavorite } from '../../services/favoriteService';
import { showToast } from '../../utils/toastService';
import { Loader } from '../common/Loader';
import { useOptimisticLike } from '../../hooks/useOptimisticLike';

const PhotoModal = ({ photoId, onClose, onPhotoUpdate }) => {
  const [photoDetail, setPhotoDetail] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [newComment, setNewComment] = useState('');
  const [isSubmittingComment, setIsSubmittingComment] = useState(false);
  const [comments, setComments] = useState([]);
  const [isSaved, setIsSaved] = useState(false);
  const [isSaving, setIsSaving] = useState(false);

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
        setComments(response.comments || []);
        setIsSaved(response.isSavedByCurrentUser ?? false);
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

  const handleSubmitComment = async (e) => {
    e.preventDefault();
    
    if (!newComment.trim() || isSubmittingComment) return;
    
    setIsSubmittingComment(true);
    
    try {
      const response = await createComment(photoId, { content: newComment.trim() });
      // Ensure createdAt exists so formatDate doesn't produce "Invalid Date"
      const newCommentEntry = {
        ...response,
        createdAt: response.createdAt ?? new Date().toISOString(),
      };
      const updatedComments = [...comments, newCommentEntry];
      setComments(updatedComments);
      setNewComment('');
      
      if (onPhotoUpdate && photoDetail) {
        const updatedPhoto = {
          ...photoDetail,
          commentCount: updatedComments.length,
          isLikedByCurrentUser: isLiked,
          likeCount: likesCount
        };
        onPhotoUpdate(photoId, updatedPhoto);
      }
      
      showToast('success', 'Đã thêm bình luận');
    } catch (error) {
      showToast('error', 'Không thể thêm bình luận. Vui lòng thử lại.');
    } finally {
      setIsSubmittingComment(false);
    }
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
                src={photoDetail.userImageUrl} 
                alt={photoDetail.username} 
                className="user-avatar"
              />
              <span className="username">{photoDetail.username}</span>
            </div>

            {/* Caption and Comments */}
            <div className="photo-modal-content-area">
              {/* Caption */}
              {photoDetail.caption && (
                <div className="photo-modal-caption">
                  <img 
                    src={photoDetail.userImageUrl} 
                    alt={photoDetail.username} 
                    className="user-avatar-small"
                  />
                  <div className="caption-content">
                    <span className="username">{photoDetail.username}</span>
                    <p className="caption-text">{photoDetail.caption}</p>
                    <span className="timestamp">{formatDate(photoDetail.createdAt)}</span>
                  </div>
                </div>
              )}

              {/* Comments */}
              <div className="photo-modal-comments">
                {comments && comments.length > 0 ? (
                  comments.map((comment, index) => (
                    <div key={comment.id ?? index} className="comment-item">
                      <img 
                        src={comment.userImageUrl} 
                        alt={comment.username} 
                        className="user-avatar-small"
                      />
                      <div className="comment-content">
                        <span className="username">{comment.username}</span>
                        <p className="comment-text">{comment.text}</p>
                        <span className="timestamp">{formatDate(comment.createdAt)}</span>
                      </div>
                    </div>
                  ))
                ) : (
                  <p className="no-comments">Chưa có bình luận nào</p>
                )}
              </div>
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

              {/* Add Comment Input */}
              <form onSubmit={handleSubmitComment} className="comment-form">
                <input
                  id="modal-comment-input"
                  name="comment"
                  type="text"
                  placeholder="Thêm bình luận..."
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
      </div>
    </div>
  );
};

export default PhotoModal;
