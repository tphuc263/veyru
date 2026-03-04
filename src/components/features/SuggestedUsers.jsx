import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { getSuggestedUsers } from '../../services/recommendationService';
import { follow } from '../../services/followService';
import { useAuthContext } from '../../context/AuthContext';
import { getCurrentUserProfile } from '../../services/userService';
import '../../assets/styles/components/suggestedUsers.css';

const SuggestedUsers = () => {
    const { user: currentUser } = useAuthContext();
    const [currentUserProfile, setCurrentUserProfile] = useState(null);
    const [suggestedUsers, setSuggestedUsers] = useState([]);
    const [loading, setLoading] = useState(true);
    const [followingSet, setFollowingSet] = useState(new Set());
    const [showAll, setShowAll] = useState(false);

    useEffect(() => {
        loadSuggestions();
        loadCurrentUserProfile();
    }, []);

    const loadCurrentUserProfile = async () => {
        try {
            const profile = await getCurrentUserProfile();
            setCurrentUserProfile(profile);
        } catch (err) {
            console.error('Failed to load current user profile:', err);
        }
    };

    const loadSuggestions = async () => {
        try {
            setLoading(true);
            const users = await getSuggestedUsers(20);
            setSuggestedUsers(users);
        } catch (err) {
            console.error('Failed to load suggested users:', err);
        } finally {
            setLoading(false);
        }
    };

    const handleFollow = async (targetUserId) => {
        try {
            setFollowingSet(prev => new Set([...prev, targetUserId]));
            await follow(targetUserId);
        } catch (err) {
            setFollowingSet(prev => {
                const next = new Set(prev);
                next.delete(targetUserId);
                return next;
            });
            console.error('Failed to follow:', err);
        }
    };

    const displayedUsers = showAll ? suggestedUsers : suggestedUsers.slice(0, 5);

    if (loading) {
        return (
            <div className="suggested-users-container">
                <div className="suggested-header">
                    <span className="suggested-title">Suggested for you</span>
                </div>
                <div className="suggested-loading">
                    <div className="suggested-skeleton">
                        {[1, 2, 3, 4, 5].map(i => (
                            <div key={i} className="suggested-skeleton-item">
                                <div className="skeleton-avatar" />
                                <div className="skeleton-text">
                                    <div className="skeleton-line short" />
                                    <div className="skeleton-line" />
                                </div>
                            </div>
                        ))}
                    </div>
                </div>
            </div>
        );
    }

    if (suggestedUsers.length === 0) {
        return null;
    }

    return (
        <div className="suggested-users-container">
            {/* Current user profile mini */}
            {currentUser && (
                <Link to="/profile" className="current-user-mini">
                    <img
                        src={currentUserProfile?.imageUrl || '/default-avatar.png'}
                        alt={currentUser.username}
                        className="current-user-avatar"
                    />
                    <div className="current-user-info">
                        <span className="current-user-username">{currentUser.username}</span>
                        <span className="current-user-name">{currentUserProfile?.bio || currentUser.email}</span>
                    </div>
                </Link>
            )}

            {/* Suggested header */}
            <div className="suggested-header">
                <span className="suggested-title">Suggested for you</span>
                {suggestedUsers.length > 5 && (
                    <button
                        className="suggested-see-all"
                        onClick={() => setShowAll(!showAll)}
                    >
                        {showAll ? 'Show less' : 'See All'}
                    </button>
                )}
            </div>

            {/* User list */}
            <div className="suggested-list">
                {displayedUsers.map(user => (
                    <div key={user.id} className="suggested-user-item">
                        <Link to={`/profile/${user.id}`} className="suggested-user-link">
                            <img
                                src={user.imageUrl || '/default-avatar.png'}
                                alt={user.username}
                                className="suggested-user-avatar"
                            />
                            <div className="suggested-user-info">
                                <span className="suggested-user-username">{user.username}</span>
                                <span className="suggested-user-reason">{user.reason}</span>
                            </div>
                        </Link>
                        {followingSet.has(user.id) ? (
                            <span className="suggested-following-label">Following</span>
                        ) : (
                            <button
                                className="suggested-follow-btn"
                                onClick={() => handleFollow(user.id)}
                            >
                                Follow
                            </button>
                        )}
                    </div>
                ))}
            </div>

            {/* Footer */}
            <div className="suggested-footer">
                <p>© 2026 SHARE APP</p>
            </div>
        </div>
    );
};

export default SuggestedUsers;
