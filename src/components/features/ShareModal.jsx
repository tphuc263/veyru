import { useState } from 'react';
import { sharePhoto } from '../../services/shareService';
import { showToast } from '../../utils/toastService';
import '../../assets/styles/components/shareModal.css';

const ShareModal = ({ photoId, photoSrc, onClose, onShareSuccess }) => {
    const [caption, setCaption] = useState('');
    const [isSharing, setIsSharing] = useState(false);

    const handleShare = async () => {
        if (isSharing) return;
        setIsSharing(true);
        try {
            const response = await sharePhoto(photoId, caption.trim() || null);
            showToast('success', 'Đã chia sẻ về trang cá nhân!');
            if (onShareSuccess) {
                onShareSuccess(response.data || response);
            }
            onClose();
        } catch (err) {
            showToast('error', 'Không thể chia sẻ. Vui lòng thử lại.');
            console.error('Failed to share:', err);
        } finally {
            setIsSharing(false);
        }
    };

    const handleBackdropClick = (e) => {
        if (e.target.classList.contains('share-modal-backdrop')) {
            onClose();
        }
    };

    return (
        <div className="share-modal-backdrop" onClick={handleBackdropClick}>
            <div className="share-modal">
                <div className="share-modal-header">
                    <h3>Chia sẻ bài viết</h3>
                    <button className="share-modal-close" onClick={onClose}>✕</button>
                </div>

                <div className="share-modal-body">
                    <div className="share-preview">
                        <img src={photoSrc} alt="Ảnh chia sẻ" className="share-preview-image" />
                    </div>

                    <div className="share-caption-section">
                        <textarea
                            className="share-caption-input"
                            placeholder="Viết gì đó về bài viết này..."
                            value={caption}
                            onChange={(e) => setCaption(e.target.value)}
                            maxLength={500}
                            rows={3}
                        />
                        <span className="share-caption-count">{caption.length}/500</span>
                    </div>
                </div>

                <div className="share-modal-footer">
                    <button
                        className="share-btn-cancel"
                        onClick={onClose}
                        disabled={isSharing}
                    >
                        Hủy
                    </button>
                    <button
                        className="share-btn-confirm"
                        onClick={handleShare}
                        disabled={isSharing}
                    >
                        {isSharing ? (
                            <span className="share-btn-loading">
                                <svg className="share-spinner" width="16" height="16" viewBox="0 0 16 16" fill="none">
                                    <circle cx="8" cy="8" r="6" stroke="currentColor" strokeWidth="2" strokeDasharray="28" strokeDashoffset="8" />
                                </svg>
                                Đang chia sẻ...
                            </span>
                        ) : (
                            <>
                                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                                    <line x1="22" y1="2" x2="11" y2="13"></line>
                                    <polygon points="22 2 15 22 11 13 2 9 22 2"></polygon>
                                </svg>
                                Chia sẻ
                            </>
                        )}
                    </button>
                </div>
            </div>
        </div>
    );
};

export default ShareModal;
