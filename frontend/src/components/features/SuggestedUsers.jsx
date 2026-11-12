import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { getSuggestedUsers } from '../../services/recommendationService';
import { follow } from '../../services/followService';
import { useAuthContext } from '../../context/AuthContext';
import { getCurrentUserProfile } from '../../services/userService';
import { DEFAULT_AVATAR } from '../../utils/constants';
import { optimizeImageUrl } from '../../utils/helpers';
import { pageCache } from '../../utils/pageCache';
import '../../assets/styles/components/suggestedUsers.css';

const SuggestedUsers = () => {
    const cached = pageCache.getSuggestedUsers();
    const { user: currentUser } = useAuthContext();
    const [currentUserProfile, setCurrentUserProfile] = useState(() => cached?.currentUserProfile || null);
    const [suggestedUsers, setSuggestedUsers] = useState(() => cached?.suggestedUsers || []);
    const [loading, setLoading] = useState(() => !cached || cached.suggestedUsers.length === 0);
    const [followingSet, setFollowingSet] = useState(() => new Set(cached?.followingIds || []));
    const [showAll, setShowAll] = useState(false);

    useEffect(() => {
        const currentCached = pageCache.getSuggestedUsers();
        if (pageCache.isSuggestedUsersFresh() && (currentCached?.suggestedUsers.length ?? 0) > 0) {
            setLoading(false);
            return;
        }
        const silent = (currentCached?.suggestedUsers.length ?? 0) > 0;
        Promise.allSettled([getSuggestedUsers(10), getCurrentUserProfile()]).then(([usersRes, profileRes]) => {
            const latestCached = pageCache.getSuggestedUsers();
            const nextUsers = usersRes.status === 'fulfilled' ? usersRes.value : (latestCached?.suggestedUsers || []);
            const nextProfile = profileRes.status === 'fulfilled' ? profileRes.value : (latestCached?.currentUserProfile || null);
            if (usersRes.status === 'fulfilled') setSuggestedUsers(nextUsers);
            if (profileRes.status === 'fulfilled') setCurrentUserProfile(nextProfile);
            pageCache.setSuggestedUsers({
                suggestedUsers: nextUsers,
                currentUserProfile: nextProfile,
                followingIds: latestCached?.followingIds || []
            });
            setLoading(false);
        });
        if (!silent) {
            setLoading(true);
        }
    }, []);

    const handleFollow = async (targetUserId) => {
        try {
            setFollowingSet(prev => {
                const next = new Set([...prev, targetUserId]);
                const existing = pageCache.getSuggestedUsers();
                if (existing) {
                    pageCache.setSuggestedUsers({ ...existing, followingIds: Array.from(next) });
                }
                return next;
            });
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
                        src={optimizeImageUrl(currentUserProfile?.imageUrl, 120) || DEFAULT_AVATAR}
                        alt={currentUser.username}
                        className="current-user-avatar"
                        loading="lazy"
                        decoding="async"
                        onError={e => { e.currentTarget.src = DEFAULT_AVATAR; }}
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
                {displayedUsers.map(user => {
                    const rawSubtitle = (user.bio && user.bio.trim()) ? user.bio.trim() : 'Gợi ý cho bạn';
                    const subtitle = rawSubtitle.length > 42 ? `${rawSubtitle.slice(0, 42).trim()}…` : rawSubtitle;
                    return (
                    <div key={user.id} className="suggested-user-item">
                        <Link to={`/profile/${user.id}`} className="suggested-user-link">
                            <img
                                src={optimizeImageUrl(user.imageUrl, 120) || DEFAULT_AVATAR}
                                alt={user.username}
                                className="suggested-user-avatar"
                                loading="lazy"
                                decoding="async"
                                onError={e => { e.currentTarget.src = DEFAULT_AVATAR; }}
                            />
                            <div className="suggested-user-info">
                                <span className="suggested-user-username">{user.username}</span>
                                <span className="suggested-user-reason" title={user.bio || ''}>
                                    {subtitle}
                                </span>
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
                    );
                })}
            </div>

            {/* Footer */}
            <div className="suggested-footer">
                <p>© 2026 VEYRU</p>
            </div>
        </div>
    );
};

export default SuggestedUsers;
