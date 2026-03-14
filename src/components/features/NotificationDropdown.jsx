import { useState, useEffect, useRef } from 'react';
import { Bell, Heart, MessageCircle, UserPlus, AtSign, ImagePlus, Share2 } from 'lucide-react';
import { getNotifications, getUnreadCount, markAsRead, markAllAsRead } from '../../services/notificationService';
import { showToast } from '../../utils/toastService';
import { DEFAULT_AVATAR } from '../../utils/constants';
import '../../assets/styles/components/notificationDropdown.css';

const NotificationIcon = ({ type }) => {
  switch (type) {
    case 'LIKE_PHOTO':
    case 'LIKE_COMMENT':
      return <Heart size={16} className="notification-icon like" fill="#ed4956" stroke="#ed4956" />;
    case 'COMMENT_PHOTO':
    case 'REPLY_COMMENT':
      return <MessageCircle size={16} className="notification-icon comment" />;
    case 'NEW_FOLLOWER':
      return <UserPlus size={16} className="notification-icon follow" />;
    case 'MENTION_IN_COMMENT':
      return <AtSign size={16} className="notification-icon mention" />;
    case 'TAG_IN_PHOTO':
      return <ImagePlus size={16} className="notification-icon tag" />;
    case 'SHARE_PHOTO':
      return <Share2 size={16} className="notification-icon forward" />;
    default:
      return <Bell size={16} className="notification-icon" />;
  }
};

const NotificationItem = ({ notification, onRead, onNavigate }) => {
  const handleClick = () => {
    if (!notification.read) {
      onRead(notification.id);
    }
    onNavigate(notification);
  };

  const formatTime = (dateString) => {
    const date = new Date(dateString);
    const now = new Date();
    const diffMs = now - date;
    const diffMins = Math.floor(diffMs / 60000);
    const diffHours = Math.floor(diffMs / 3600000);
    const diffDays = Math.floor(diffMs / 86400000);

    if (diffMins < 1) return 'vừa xong';
    if (diffMins < 60) return `${diffMins} phút`;
    if (diffHours < 24) return `${diffHours} giờ`;
    if (diffDays < 7) return `${diffDays} ngày`;
    return date.toLocaleDateString('vi-VN');
  };

  return (
    <div 
      className={`notification-item ${!notification.read ? 'unread' : ''}`}
      onClick={handleClick}
    >
      <div className="notification-avatar-container">
        <img
          src={notification.actorImageUrl || DEFAULT_AVATAR}
          alt={notification.actorUsername}
          className="notification-avatar"
          onError={e => { e.currentTarget.src = DEFAULT_AVATAR; }}
        />
        <div className="notification-type-icon">
          <NotificationIcon type={notification.type} />
        </div>
      </div>
      
      <div className="notification-content">
        <p className="notification-message">
          <strong>{notification.actorUsername}</strong> {notification.message?.replace(notification.actorUsername, '').trim()}
        </p>
        <span className="notification-time">{formatTime(notification.createdAt)}</span>
      </div>
      
      {notification.thumbnailUrl && (
        <img 
          src={notification.thumbnailUrl} 
          alt="" 
          className="notification-thumbnail"
        />
      )}
      
      {!notification.read && <div className="notification-unread-dot" />}
    </div>
  );
};

const NotificationDropdown = ({ onPhotoClick, isControlled = false, onClose }) => {
  const [isOpen, setIsOpen] = useState(false);
  const [notifications, setNotifications] = useState([]);
  const [unreadCount, setUnreadCount] = useState(0);
  const [loading, setLoading] = useState(false);
  const [page, setPage] = useState(0);
  const [hasMore, setHasMore] = useState(true);
  const dropdownRef = useRef(null);

  // Use controlled or internal open state
  const effectiveIsOpen = isControlled ? true : isOpen;

  useEffect(() => {
    fetchUnreadCount();
    
    // Poll for new notifications every 30 seconds
    const interval = setInterval(fetchUnreadCount, 30000);
    return () => clearInterval(interval);
  }, []);

  useEffect(() => {
    // Fetch notifications when opened (in controlled mode, fetch immediately)
    if (isControlled) {
      fetchNotifications(true);
    }
  }, [isControlled]);

  useEffect(() => {
    const handleClickOutside = (event) => {
      if (dropdownRef.current && !dropdownRef.current.contains(event.target)) {
        if (!isControlled) {
          setIsOpen(false);
        }
      }
    };

    if (!isControlled) {
      document.addEventListener('mousedown', handleClickOutside);
      return () => document.removeEventListener('mousedown', handleClickOutside);
    }
  }, [isControlled]);

  const fetchUnreadCount = async () => {
    try {
      const response = await getUnreadCount();
      setUnreadCount(response.data ?? response ?? 0);
    } catch (error) {
      console.error('Failed to fetch unread count:', error);
    }
  };

  const fetchNotifications = async (reset = false) => {
    if (loading) return;
    
    setLoading(true);
    try {
      const currentPage = reset ? 0 : page;
      const response = await getNotifications(currentPage, 20);
      const newNotifications = response.data ?? response ?? [];
      
      if (reset) {
        setNotifications(newNotifications);
        setPage(1);
      } else {
        setNotifications(prev => [...prev, ...newNotifications]);
        setPage(prev => prev + 1);
      }
      
      setHasMore(newNotifications.length === 20);
    } catch (error) {
      console.error('Failed to fetch notifications:', error);
      showToast('error', 'Không thể tải thông báo');
    } finally {
      setLoading(false);
    }
  };

  const handleToggle = () => {
    if (!isOpen) {
      fetchNotifications(true);
    }
    setIsOpen(!isOpen);
  };

  const handleRead = async (notificationId) => {
    try {
      await markAsRead(notificationId);
      setNotifications(prev => 
        prev.map(n => n.id === notificationId ? { ...n, read: true } : n)
      );
      setUnreadCount(prev => Math.max(0, prev - 1));
    } catch (error) {
      console.error('Failed to mark as read:', error);
    }
  };

  const handleMarkAllRead = async () => {
    try {
      await markAllAsRead();
      setNotifications(prev => prev.map(n => ({ ...n, read: true })));
      setUnreadCount(0);
      showToast('success', 'Đã đánh dấu tất cả là đã đọc');
    } catch (error) {
      showToast('error', 'Không thể đánh dấu đã đọc');
    }
  };

  const handleNavigate = (notification) => {
    if (isControlled && onClose) {
      onClose();
    } else {
      setIsOpen(false);
    }
    
    if (notification.photoId && onPhotoClick) {
      onPhotoClick(notification.photoId);
    } else if (notification.type === 'NEW_FOLLOWER' && notification.actorId) {
      // Navigate to user profile
      window.location.href = `/profile/${notification.actorId}`;
    }
  };

  const handleScroll = (e) => {
    const { scrollTop, scrollHeight, clientHeight } = e.target;
    if (scrollHeight - scrollTop <= clientHeight * 1.5 && hasMore && !loading) {
      fetchNotifications();
    }
  };

  return (
    <div className={`notification-dropdown-container ${isControlled ? 'controlled' : ''}`} ref={dropdownRef}>
      {!isControlled && (
        <button 
          className="notification-trigger"
          onClick={handleToggle}
          aria-label="Notifications"
        >
          <Bell size={24} />
          {unreadCount > 0 && (
            <span className="notification-badge">
              {unreadCount > 99 ? '99+' : unreadCount}
            </span>
          )}
        </button>
      )}

      {effectiveIsOpen && (
        <div className="notification-dropdown">
          <div className="notification-header">
            <h3>Thông báo</h3>
            {unreadCount > 0 && (
              <button onClick={handleMarkAllRead} className="mark-all-read-btn">
                Đánh dấu tất cả đã đọc
              </button>
            )}
          </div>
          
          <div className="notification-list" onScroll={handleScroll}>
            {notifications.length === 0 && !loading ? (
              <div className="no-notifications">
                <Bell size={48} strokeWidth={1} />
                <p>Chưa có thông báo nào</p>
              </div>
            ) : (
              <>
                {notifications.map(notification => (
                  <NotificationItem
                    key={notification.id}
                    notification={notification}
                    onRead={handleRead}
                    onNavigate={handleNavigate}
                  />
                ))}
                {loading && (
                  <div className="notification-loading">
                    <div className="loading-spinner" />
                  </div>
                )}
              </>
            )}
          </div>
        </div>
      )}
    </div>
  );
};

export default NotificationDropdown;
