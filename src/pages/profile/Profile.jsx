import { useState, useEffect } from "react";
import { Link, useNavigate } from "react-router-dom";
import { DEFAULT_AVATAR } from "../../utils/constants";
import "../../assets/styles/pages/profilePage.css";
import { useAuthContext } from "../../context/AuthContext.jsx";
import { useUserProfile } from "../../hooks/useUserProfile.js";
import { follow, unfollow } from "../../services/followService";
import { getFavorites } from "../../services/favoriteService";
import {toastSuccess, toastError} from '../../utils/toastService.js';
import PhotoModal from "../../components/features/PhotoModal.jsx";
import FollowListModal from "../../components/features/FollowListModal.jsx";

const ProfilePage = () => {
  const { profile, posts, handleLoadMore, setProfile, setPosts } = useUserProfile();
  const { user: currentUser } = useAuthContext();
  const navigate = useNavigate();

  const [isFollowLoading, setIsFollowLoading] = useState(false);
  const [selectedPhotoId, setSelectedPhotoId] = useState(null);
  const [followModal, setFollowModal] = useState({ open: false, type: null });
  const [activeTab, setActiveTab] = useState('posts');
  const [favorites, setFavorites] = useState([]);
  const [favoritesLoading, setFavoritesLoading] = useState(false);

  const isOwnProfile =
    currentUser && profile.data && currentUser.id === profile.data.id;

  useEffect(() => {
    if (activeTab === 'saved' && isOwnProfile) {
      const fetchFavorites = async () => {
        setFavoritesLoading(true);
        try {
          const data = await getFavorites(0, 50);
          setFavorites(data);
        } catch (err) {
          console.error('Failed to load favorites:', err);
        } finally {
          setFavoritesLoading(false);
        }
      };
      fetchFavorites();
    }
  }, [activeTab, isOwnProfile]);

  const handlePhotoClick = (photoId) => {
    setSelectedPhotoId(photoId);
  };

  const handleCloseModal = () => {
    setSelectedPhotoId(null);
  };

  if (profile.loading) {
    return (
      <div className="profile-container">
        <p>Đang tải trang cá nhân...</p>
      </div>
    );
  }

  if (profile.error) {
    return (
      <div className="profile-container">
        <p>Lỗi: {profile.error}</p>
      </div>
    );
  }

  if (!profile.data) {
    return (
      <div className="profile-container">
        <p>Không tìm thấy dữ liệu người dùng.</p>
      </div>
    );
  }

  const displayData = profile.data;
  const stats = profile.data.stats;

  const showEditButton = isOwnProfile;
  const showFollowButton = currentUser && !isOwnProfile;

  const handleFollowToggle = async () => {
    if (!profile.data?.id) return;

    setIsFollowLoading(true);

    const currentlyFollowing = profile.data.followingByCurrentUser;

    try {
      if (currentlyFollowing) {
        await unfollow(profile.data.id);

        setProfile((prevProfile) => ({
          ...prevProfile,
          data: {
            ...prevProfile.data,
            followingByCurrentUser: false,
            stats: {
              ...prevProfile.data.stats,
              followers: prevProfile.data.stats.followers - 1,
            },
          },
        }));
        toastSuccess.unfollowed();
      } else {
        await follow(profile.data.id);

        setProfile((prevProfile) => ({
          ...prevProfile,
          data: {
            ...prevProfile.data,
            followingByCurrentUser: true,
            stats: {
              ...prevProfile.data.stats,
              followers: prevProfile.data.stats.followers + 1,
            },
          },
        }));
        toastSuccess.followed();
      }
    } catch (error) {
      console.error("Failed to toggle follow state:", error);
      toastError.general();
    } finally {
      setIsFollowLoading(false);
    }
  };

  return (
    <div className="profile-container">
      <header className="profile-header">
        <div className="profile-avatar">
          <div className="avatar-wrapper">
            <img
              src={displayData.imageUrl || DEFAULT_AVATAR}
              alt="Avatar"
              onError={e => { e.currentTarget.src = DEFAULT_AVATAR; }}
            />
          </div>
        </div>
        <div className="profile-info">
          <div className="profile-info-top">
            <h1 className="username">{displayData.username}</h1>
            {showEditButton && (
              <Link to="/edit-profile" className="edit-profile-button">
                Edit Profile
              </Link>
            )}
            {showFollowButton && (
              <button
                className={`follow-button ${
                  profile.data.followingByCurrentUser ? "following" : ""
                }`}
                onClick={handleFollowToggle}
                disabled={isFollowLoading}
              >
                {isFollowLoading
                  ? "..."
                  : profile.data.followingByCurrentUser
                  ? "Following"
                  : "Follow"}
              </button>
            )}
            {showFollowButton && (
              <button
                className="message-profile-button"
                onClick={() => navigate(`/messages?userId=${profile.data.id}`)}
              >
                Message
              </button>
            )}
          </div>
          <div className="profile-stats">
            <span>
              <strong>{stats?.posts}</strong> posts
            </span>
            <span 
              className="stat-clickable"
              onClick={() => setFollowModal({ open: true, type: 'followers' })}
            >
              <strong>{stats?.followers}</strong> followers
            </span>
            <span 
              className="stat-clickable"
              onClick={() => setFollowModal({ open: true, type: 'following' })}
            >
              <strong>{stats?.following}</strong> following
            </span>
          </div>
          <div className="profile-bio">
            <p>{displayData.bio}</p>
          </div>
        </div>
      </header>

      <main className="profile-posts-container">
        {/* Instagram-style tabs */}
        <div className="profile-tabs">
          <button 
            className={`profile-tab ${activeTab === 'posts' ? 'active' : ''}`}
            onClick={() => setActiveTab('posts')}
          >
            <svg width="12" height="12" viewBox="0 0 24 24" fill="currentColor">
              <rect x="2" y="2" width="9" height="9" rx="1"/>
              <rect x="13" y="2" width="9" height="9" rx="1"/>
              <rect x="2" y="13" width="9" height="9" rx="1"/>
              <rect x="13" y="13" width="9" height="9" rx="1"/>
            </svg>
            <span>POSTS</span>
          </button>
          {isOwnProfile && (
            <button 
              className={`profile-tab ${activeTab === 'saved' ? 'active' : ''}`}
              onClick={() => setActiveTab('saved')}
            >
              <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                <path d="M19 21l-7-5-7 5V5a2 2 0 0 1 2-2h10a2 2 0 0 1 2 2z"></path>
              </svg>
              <span>SAVED</span>
            </button>
          )}
        </div>

        {/* Posts Grid */}
        {activeTab === 'posts' && (
          <>
            <div className="profile-posts">
              {posts.data.map((post, index) => (
                <div 
                  key={post.id || index} 
                  className="post-item"
                  onClick={() => handlePhotoClick(post.id)}
                >
                  <img src={post.imageUrl} alt={`Post ${post.id}`} />
                  <div className="post-overlay">
                    <div className="post-stats">
                      <span>❤️ {post.likeCount || 0}</span>
                      <span>💬 {post.commentCount || 0}</span>
                    </div>
                  </div>
                </div>
              ))}
            </div>

            {posts.error && (
              <p className="error-message">Lỗi khi tải bài viết: {posts.error}</p>
            )}

            {posts.loading && <p>Đang tải thêm bài viết...</p>}
            {!posts.loading && posts.currentPage < posts.totalPages - 1 && (
              <button onClick={handleLoadMore} className="load-more-button">
                Tải thêm
              </button>
            )}
          </>
        )}

        {/* Saved/Favorites Grid */}
        {activeTab === 'saved' && isOwnProfile && (
          <>
            {favoritesLoading ? (
              <p style={{ textAlign: 'center', padding: '40px 0', color: '#8e8e8e' }}>Đang tải...</p>
            ) : favorites.length === 0 ? (
              <div className="empty-saved">
                <svg width="62" height="62" viewBox="0 0 24 24" fill="none" stroke="#8e8e8e" strokeWidth="1">
                  <path d="M19 21l-7-5-7 5V5a2 2 0 0 1 2-2h10a2 2 0 0 1 2 2z"></path>
                </svg>
                <h3>Save</h3>
                <p>Save photos that you want to see again. No one is notified, and only you can see what you've saved.</p>
              </div>
            ) : (
              <div className="profile-posts">
                {favorites.map((photo, index) => (
                  <div 
                    key={photo.id || index} 
                    className="post-item"
                    onClick={() => handlePhotoClick(photo.id)}
                  >
                    <img src={photo.imageUrl} alt={`Saved ${photo.id}`} />
                    <div className="post-overlay">
                      <div className="post-stats">
                        <span>❤️ {photo.likeCount || 0}</span>
                        <span>💬 {photo.commentCount || 0}</span>
                      </div>
                    </div>
                  </div>
                ))}
              </div>
            )}
          </>
        )}
      </main>

      {selectedPhotoId && (() => (
          <PhotoModal 
            photoId={selectedPhotoId}
            onClose={handleCloseModal}
            onPhotoUpdate={(photoId, updatedPhoto) => {
              // Sync state between modal and profile posts
              setPosts(prevPosts => ({
                ...prevPosts,
                data: prevPosts.data.map(post => 
                  post.id === photoId 
                    ? { 
                        ...post, 
                        isLikedByCurrentUser: updatedPhoto.isLikedByCurrentUser,
                        likeCount: updatedPhoto.likeCount,
                        commentCount: updatedPhoto.commentCount || post.commentCount
                      }
                    : post
                )
              }));
            }}
          />
      ))()} 

      {followModal.open && (
        <FollowListModal
          userId={profile.data.id}
          type={followModal.type}
          onClose={() => setFollowModal({ open: false, type: null })}
          onFollowChange={() => {
            // Re-fetch profile to update follower/following counts
            // Simple approach: just refresh the count by modifying state
          }}
        />
      )}
    </div>
  );
};

export default ProfilePage;
