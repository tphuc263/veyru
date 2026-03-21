import {useEffect, useState} from "react";
import {useSearchParams} from "react-router-dom";
import {getNewsfeed} from "../../services/newsfeedService";
import PhotoCard from '../../components/features/PhotoCard.jsx';
import PhotoModal from '../../components/features/PhotoModal.jsx';
import SuggestedUsers from '../../components/features/SuggestedUsers.jsx';
import {Loader} from '../../components/common/Loader.jsx'
import '../../assets/styles/pages/homePage.css';


const Home = () => {
    const [searchParams, setSearchParams] = useSearchParams();
    const [feed, setFeed] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [page, setPage] = useState(0);
    const [hasMore, setHasMore] = useState(true);
    const [selectedPhotoId, setSelectedPhotoId] = useState(null);
    const [showScrollTop, setShowScrollTop] = useState(false);

    // Restore photo modal from URL on page load
    useEffect(() => {
        const photoId = searchParams.get('photo');
        if (photoId && !selectedPhotoId) {
            setSelectedPhotoId(photoId);
        }
    }, [searchParams]);

    // Update URL when selectedPhotoId changes
    const handlePhotoSelect = (photoId) => {
        setSelectedPhotoId(photoId);
        if (photoId) {
            setSearchParams({ photo: photoId }, { replace: true });
        }
    };

    const handlePhotoClose = () => {
        setSelectedPhotoId(null);
        setSearchParams({}, { replace: true });
    };

    // Handle scroll to show/hide scroll-to-top button
    useEffect(() => {
        const handleScroll = () => {
            setShowScrollTop(window.scrollY > 400);
        };
        window.addEventListener('scroll', handleScroll);
        return () => window.removeEventListener('scroll', handleScroll);
    }, []);

    const scrollToTop = () => {
        window.scrollTo({ top: 0, behavior: 'smooth' });
    };

    const loadFeed = async (pageNum = 0, isRefresh = false) => {
        try {
            setLoading(true);
            setError(null);

            const response = await getNewsfeed(pageNum, 20);
            const newPosts = response.content || [];

            if (isRefresh) {
                setFeed(newPosts);
            } else {
                setFeed(prev => [...prev, ...newPosts]);
            }

            setHasMore(response.page < response.totalPages - 1);
            setPage(pageNum);
        } catch (err) {
            setError(err.message);
        } finally {
            setLoading(false);
        }
    };

    const loadMore = () => {
        if (!loading && hasMore) {
            loadFeed(page + 1, false);
        }
    };

    const refreshFeed = () => {
        loadFeed(0, true).catch(err => {
            console.error('Failed to load initial feed:', err);
        });
    };

    useEffect(() => {
        loadFeed(0, true).catch(err => {
            console.error('Failed to load initial feed:', err);
        });
    }, []);


    return (
        <div className="home-container">
            <main className="feed-wrapper">
                {error && (
                    <div className="error-message">
                        <svg width="96" height="96" viewBox="0 0 96 96" fill="none">
                            <circle cx="48" cy="48" r="48" fill="#f0f0f0"/>
                            <path d="M48 32v24M48 64h.01" stroke="#8e8e8e" strokeWidth="4" strokeLinecap="round"/>
                        </svg>
                        <h3>Không thể tải bài viết</h3>
                        <p>{error}</p>
                        <button onClick={refreshFeed} className="retry-btn">Thử lại</button>
                    </div>
                )}
                {loading && feed.length === 0 && !error && (
                    <div className="page-loader-container">
                        <Loader />
                    </div>
                )}

                {!loading && !error && feed.length === 0 && (
                    <div className="empty-feed">
                        <svg width="96" height="96" viewBox="0 0 96 96" fill="none">
                            <circle cx="48" cy="48" r="40" stroke="#dbdbdb" strokeWidth="3"/>
                            <path d="M48 28v40M28 48h40" stroke="#dbdbdb" strokeWidth="3" strokeLinecap="round"/>
                        </svg>
                        <h3>Chưa có bài viết nào</h3>
                        <p>Hãy theo dõi người dùng khác để xem bài viết của họ</p>
                    </div>
                )}

                {feed.map((post) => {
                    // Determine if this is a SHARE post or PHOTO post
                    const isShare = post.type === 'SHARE';
                    // For SHARE type, use original photo info
                    const displayPhotoId = isShare ? post.originalPhotoId : post.id;
                    const displayImageUrl = isShare ? post.originalImageUrl : post.imageUrl;
                    const displayCaption = isShare ? post.shareCaption : post.caption;
                    const displayUsername = isShare ? post.originalUsername : post.username;
                    const displayUserImageUrl = isShare ? post.originalUserImageUrl : post.userImageUrl;
                    const displayLikesCount = isShare ? post.originalLikeCount : post.likeCount;
                    const displayCommentsCount = isShare ? post.originalCommentCount : post.commentCount;
                    const displayShareCount = isShare ? post.originalShareCount : post.shareCount;

                    return (
                            <PhotoCard
                            key={post.id}
                            photoId={displayPhotoId}
                            username={post.username}
                            avatarSrc={post.userImageUrl}
                            photoSrc={displayImageUrl}
                            caption={displayCaption}
                            likesCount={displayLikesCount}
                            commentsCount={displayCommentsCount}
                            sharesCount={displayShareCount || 0}
                            isLiked={post.isLikedByCurrentUser}
                            isSaved={post.isSavedByCurrentUser}
                            createdAt={post.createdAt}
                            tags={post.tags}
                            isSharePost={isShare}
                            onPhotoClick={() => handlePhotoSelect(displayPhotoId)}
                            onPhotoUpdate={(photoId, updatedPhoto) => {
                                setFeed(prevFeed =>
                                    prevFeed.map(p =>
                                        p.id === post.id || p.originalPhotoId === photoId
                                            ? {
                                                ...p,
                                                isLikedByCurrentUser: updatedPhoto.isLikedByCurrentUser,
                                                likeCount: updatedPhoto.likeCount,
                                                commentCount: updatedPhoto.commentCount || p.commentCount,
                                                shareCount: updatedPhoto.shareCount ?? p.shareCount
                                              }
                                            : p
                                    )
                                );
                            }}
                        />
                    );
                })}

                {hasMore && (
                    <div className="load-more-section">
                        {loading && feed.length > 0 ? (
                            <div className="load-more-loader">
                                <Loader />
                            </div>
                        ) : (
                            <button onClick={loadMore} className="load-more-btn" disabled={loading}>
                                <svg width="20" height="20" viewBox="0 0 20 20" fill="currentColor">
                                    <path d="M10 3v14m-7-7h14" stroke="currentColor" strokeWidth="2" strokeLinecap="round"/>
                                </svg>
                                Xem thêm bài viết
                            </button>
                        )}
                    </div>
                )}

                {!hasMore && feed.length > 0 && (
                    <div className="end-of-feed">
                        <p>Bạn đã xem hết bài viết</p>
                    </div>
                )}
            </main>

            <aside className="home-sidebar">
                <SuggestedUsers />
            </aside>

            {showScrollTop && (
                <button onClick={scrollToTop} className="scroll-to-top" title="Về đầu trang">
                    <svg width="24" height="24" viewBox="0 0 24 24" fill="currentColor">
                        <path d="M12 4l-8 8h6v8h4v-8h6z"/>
                    </svg>
                </button>
            )}

            {selectedPhotoId && (() => (
                    <PhotoModal 
                        photoId={selectedPhotoId}
                        onClose={handlePhotoClose}
                        onPhotoUpdate={(photoId, updatedPhoto) => {
                            // Sync state between modal and feed
                            setFeed(prevFeed => 
                                prevFeed.map(post => 
                                    post.id === photoId 
                                        ? { 
                                            ...post, 
                                            isLikedByCurrentUser: updatedPhoto.isLikedByCurrentUser,
                                            likeCount: updatedPhoto.likeCount,
                                            commentCount: updatedPhoto.commentCount || post.commentCount,
                                            shareCount: updatedPhoto.shareCount ?? post.shareCount
                                          }
                                        : post
                                )
                            );
                        }}
                    />
            ))()} 
        </div>
    );
};

export default Home;