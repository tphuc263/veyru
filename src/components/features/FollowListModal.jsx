import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { getFollowers, getFollowing, follow, unfollow } from '../../services/followService';
import { useAuthContext } from '../../context/AuthContext';
import { Loader } from '../common/Loader';
import { DEFAULT_AVATAR } from '../../utils/constants';
import '../../assets/styles/components/followListModal.css';

const FollowListModal = ({ userId, type, onClose, onFollowChange }) => {
    const [users, setUsers] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [processingIds, setProcessingIds] = useState(new Set());
    const { user: currentUser } = useAuthContext();
    const navigate = useNavigate();

    useEffect(() => {
        const fetchUsers = async () => {
            try {
                setLoading(true);
                setError(null);
                const data = type === 'followers'
                    ? await getFollowers(userId)
                    : await getFollowing(userId);
                setUsers(data);
            } catch (err) {
                setError(err.message);
            } finally {
                setLoading(false);
            }
        };
        fetchUsers();
    }, [userId, type]);

    useEffect(() => {
        document.body.style.overflow = 'hidden';
        return () => { document.body.style.overflow = 'unset'; };
    }, []);

    const handleBackdropClick = (e) => {
        if (e.target.classList.contains('follow-modal-backdrop')) {
            onClose();
        }
    };

    const handleFollowToggle = async (targetUser) => {
        if (processingIds.has(targetUser.userId)) return;

        setProcessingIds(prev => new Set(prev).add(targetUser.userId));

        try {
            if (targetUser.followedByCurrentUser) {
                await unfollow(targetUser.userId);
            } else {
                await follow(targetUser.userId);
            }

            setUsers(prev => prev.map(u =>
                u.userId === targetUser.userId
                    ? { ...u, followedByCurrentUser: !u.followedByCurrentUser }
                    : u
            ));

            if (onFollowChange) onFollowChange();
        } catch (err) {
            console.error('Failed to toggle follow:', err);
        } finally {
            setProcessingIds(prev => {
                const next = new Set(prev);
                next.delete(targetUser.userId);
                return next;
            });
        }
    };

    const handleUserClick = (targetUserId) => {
        onClose();
        if (currentUser && String(currentUser.id) === String(targetUserId)) {
            navigate('/profile');
        } else {
            navigate(`/profile/${targetUserId}`);
        }
    };

    return (
        <div className="follow-modal-backdrop" onClick={handleBackdropClick}>
            <div className="follow-modal">
                <div className="follow-modal-header">
                    <h3>{type === 'followers' ? 'Followers' : 'Following'}</h3>
                    <button className="follow-modal-close" onClick={onClose}>✕</button>
                </div>

                <div className="follow-modal-body">
                    {loading && (
                        <div className="follow-modal-loader">
                            <Loader />
                        </div>
                    )}

                    {error && (
                        <div className="follow-modal-error">
                            <p>{error}</p>
                        </div>
                    )}

                    {!loading && !error && users.length === 0 && (
                        <div className="follow-modal-empty">
                            <p>{type === 'followers' ? 'Chưa có người theo dõi' : 'Chưa theo dõi ai'}</p>
                        </div>
                    )}

                    {!loading && !error && users.map(user => (
                        <div key={user.userId} className="follow-user-item">
                            <div 
                                className="follow-user-info"
                                onClick={() => handleUserClick(user.userId)}
                            >
                                <img
                                    src={user.userImageUrl || DEFAULT_AVATAR}
                                    alt={user.username}
                                    className="follow-user-avatar"
                                    onError={e => { e.currentTarget.src = DEFAULT_AVATAR; }}
                                />
                                <div className="follow-user-details">
                                    <span className="follow-user-username">{user.username}</span>
                                    {(user.firstName || user.lastName) && (
                                        <span className="follow-user-name">
                                            {[user.firstName, user.lastName].filter(Boolean).join(' ')}
                                        </span>
                                    )}
                                </div>
                            </div>

                            {currentUser && String(currentUser.id) !== String(user.userId) && (
                                <button
                                    className={`follow-toggle-btn ${user.followedByCurrentUser ? 'following' : ''}`}
                                    onClick={() => handleFollowToggle(user)}
                                    disabled={processingIds.has(user.userId)}
                                >
                                    {processingIds.has(user.userId)
                                        ? '...'
                                        : user.followedByCurrentUser
                                            ? 'Following'
                                            : 'Follow'
                                    }
                                </button>
                            )}
                        </div>
                    ))}
                </div>
            </div>
        </div>
    );
};

export default FollowListModal;
