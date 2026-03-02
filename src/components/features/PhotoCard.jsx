import '../../assets/styles/components/photoCard.css';
import { useOptimisticLike } from '../../hooks/useOptimisticLike';
import { useState } from 'react';
import { toggleFavorite } from '../../services/favoriteService';

const PhotoCard = ({
                       photoId,
                       username,
                       avatarSrc,
                       photoSrc,
                       caption,
                       likesCount: initialLikesCount = 0,
                       commentsCount = 0,
                       isLiked: initialIsLiked = false,
                       isSaved: initialIsSaved = false,
                       onPhotoClick,
                       onPhotoUpdate
                   }) => {
    const { isLiked, likesCount, isProcessing, handleLike } = useOptimisticLike(
        photoId,
        initialIsLiked,
        initialLikesCount,
        onPhotoUpdate
    );

    const [isSaved, setIsSaved] = useState(initialIsSaved);
    const [isSaving, setIsSaving] = useState(false);

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

    return (
        <div className="photo-card">
            <div className="photo-card-header">
                <img src={avatarSrc} alt={`${username}'s avatar`} className="avatar"/>
                <span className="username">{username}</span>
            </div>

            <img 
                src={photoSrc} 
                alt="Post" 
                className="photo"
                onClick={onPhotoClick}
                style={{ cursor: 'pointer' }}
            />

            <div className="photo-card-actions">
                <div className="action-buttons-left">
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
                    <button 
                        className="action-btn"
                        onClick={onPhotoClick}
                    >
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

            <div className="photo-card-likes">
                <strong>{likesCount}</strong> lượt thích
            </div>

            <div className="photo-card-caption">
                <span className="username">{username}</span> {caption}
            </div>

            {commentsCount > 0 && (
                <div className="photo-card-comments-count" onClick={onPhotoClick} style={{ cursor: 'pointer' }}>
                    Xem tất cả {commentsCount} bình luận
                </div>
            )}
        </div>
    );
};

export default PhotoCard;