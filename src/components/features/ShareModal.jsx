import { useState } from 'react';
import { sharePhoto } from '../../services/shareService';
import { showToast } from '../../utils/toastService';
import '../../assets/styles/components/shareModal.css';

const ShareModal = ({ photoId, photoSrc, onClose, onShareSuccess }) => {
    const [caption, setCaption] = useState('');
    const [isPosting, setIsPosting] = useState(false);

    const handlePost = async () => {
        if (isPosting) return;
        setIsPosting(true);
        try {
            const response = await sharePhoto(photoId, caption.trim() || null);
            showToast('success', 'Đã đăng về trang cá nhân!');
            if (onShareSuccess) {
                onShareSuccess(response.data || response);
            }
            onClose();
        } catch (err) {
            showToast('error', 'Không thể đăng. Vui lòng thử lại.');
            console.error('Failed to post:', err);
        } finally {
            setIsPosting(false);
        }
    };

    const handleBackdropClick = (e) => {
        if (e.target.classList.contains('post-modal-backdrop')) {
            onClose();
        }
    };

    return (
        <div className="post-modal-backdrop" onClick={handleBackdropClick}>
            <div className="post-modal">
                <div className="post-modal-header">
                    <h3>Chia sẻ bài viết</h3>
                    <button className="post-modal-close" onClick={onClose}>✕</button>
                </div>

                <div className="post-modal-body">
                    <div className="post-preview">
                        <img src={photoSrc} alt="Ảnh chia sẻ" className="post-preview-image" />
                    </div>

                    <div className="post-caption-section">
                        <textarea
                            className="post-caption-input"
                            placeholder="Viết gì đó về bài viết này..."
                            value={caption}
                            onChange={(e) => setCaption(e.target.value)}
                            maxLength={500}
                            rows={3}
                        />
                        <span className="post-caption-count">{caption.length}/500</span>
                    </div>
                </div>

                <div className="post-modal-footer">
                    <button
                        className="post-btn-cancel"
                        onClick={onClose}
                        disabled={isPosting}
                    >
                        Hủy
                    </button>
                    <button
                        className="post-btn-confirm"
                        onClick={handlePost}
                        disabled={isPosting}
                    >
                        {isPosting ? (
                            <span className="post-btn-loading">
                                <svg className="post-spinner" width="16" height="16" viewBox="0 0 16 16" fill="none">
                                    <circle cx="8" cy="8" r="6" stroke="currentColor" strokeWidth="2" strokeDasharray="28" strokeDashoffset="8" />
                                </svg>
                                Đang đăng...
                            </span>
                        ) : (
                            <>
                                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                                    <line x1="22" y1="2" x2="11" y2="13"></line>
                                    <polygon points="22 2 15 22 11 13 2 9 22 2"></polygon>
                                </svg>
                                Đăng
                            </>
                        )}
                    </button>
                </div>
            </div>
        </div>
    );
};

export default ShareModal;
