import {useEffect, useState} from "react";
import {useSearchParams} from "react-router-dom";
import {getNewsfeed} from "../../services/newsfeedService";
import {getExploreFeed} from "../../services/searchService";
import PhotoCard from '../../components/features/PhotoCard.jsx';
import PhotoModal from '../../components/features/PhotoModal.jsx';
import SuggestedUsers from '../../components/features/SuggestedUsers.jsx';
import {Loader} from '../../components/common/Loader.jsx'
import {pageCache} from '../../utils/pageCache';
import '../../assets/styles/pages/homePage.css';


const Home = () => {
    const cachedHome = pageCache.getHomeFeed();
    const [searchParams, setSearchParams] = useSearchParams();
    const [feed, setFeed] = useState(() => cachedHome?.feed || []);
    const [loading, setLoading] = useState(() => !cachedHome || cachedHome.feed.length === 0);
    const [error, setError] = useState(null);
    const [nextCursor, setNextCursor] = useState(() => cachedHome?.nextCursor ?? null);
    const [hasMore, setHasMore] = useState(() => cachedHome?.hasMore ?? true);
    const [isExploreFallback, setIsExploreFallback] = useState(() => cachedHome?.isExploreFallback ?? false);
    const [explorePage, setExplorePage] = useState(() => cachedHome?.explorePage ?? 0);
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

    const loadExploreFallback = async (page = 0, isRefresh = false) => {
        try {
            setLoading(true);
            setError(null);
            const exploreResponse = await getExploreFeed(page, 20);
            const explorePosts = (exploreResponse?.items || []).map(photo => ({
                ...photo,
                type: 'PHOTO'
            }));

            setIsExploreFallback(explorePosts.length > 0 || !isRefresh);
            setExplorePage(page);

            if (isRefresh) {
                setFeed(explorePosts);
            } else {
                setFeed(prev => [...prev, ...explorePosts]);
            }

            const moreExplore =
                typeof exploreResponse?.totalPages === 'number'
                    ? page + 1 < exploreResponse.totalPages
                    : explorePosts.length === 20;
            const nextFeed = isRefresh ? explorePosts : [...feed, ...explorePosts];
            const nextIsExplore = explorePosts.length > 0 || !isRefresh;
            setHasMore(moreExplore);
            setNextCursor(null);
            pageCache.setHomeFeed({
                feed: nextFeed,
                nextCursor: null,
                hasMore: moreExplore,
                isExploreFallback: nextIsExplore,
                explorePage: page
            });
        } catch (err) {
            setError(err.message);
        } finally {
            setLoading(false);
        }
    };

    const loadFeed = async (cursor = null, isRefresh = false, silent = false) => {
        try {
            if (!silent) {
                setLoading(true);
            }
            setError(null);
            if (isRefresh && !silent) {
                setNextCursor(null);
                setHasMore(true);
                setIsExploreFallback(false);
                setExplorePage(0);
            }

            const response = await getNewsfeed(cursor || undefined, 20);
            const newPosts = response.items || [];

            if (cursor === null && newPosts.length === 0) {
                await loadExploreFallback(0, true);
                return;
            }

            const updatedFeed = isRefresh ? newPosts : [...feed, ...newPosts];
            if (isRefresh) {
                setFeed(newPosts);
                setIsExploreFallback(false);
                setExplorePage(0);
            } else {
                setFeed(prev => [...prev, ...newPosts]);
            }

            const resolvedCursor = response.nextCursor || null;
            setNextCursor(resolvedCursor);
            setHasMore(response.hasMore);
            pageCache.setHomeFeed({
                feed: updatedFeed,
                nextCursor: resolvedCursor,
                hasMore: response.hasMore,
                isExploreFallback: false,
                explorePage: 0
            });
        } catch (err) {
            if (!isRefresh && err.response?.data?.code === 'INVALID_REQUEST_VALUE') {
                await loadFeed(null, true);
                return;
            }
            if (!silent) {
                setError(err.message);
            }
        } finally {
            setLoading(false);
        }
    };

    const loadMore = () => {
        if (loading || !hasMore) return;
        if (isExploreFallback) {
            loadExploreFallback(explorePage + 1, false);
        } else if (nextCursor) {
            loadFeed(nextCursor, false);
        }
    };

    const refreshFeed = () => {
        loadFeed(null, true).catch(err => {
            console.error('Failed to load initial feed:', err);
        });
    };

    useEffect(() => {
        if (pageCache.isHomeFeedFresh() && (pageCache.getHomeFeed()?.feed.length ?? 0) > 0) {
            setLoading(false);
            return;
        }
        const hasCachedItems = (pageCache.getHomeFeed()?.feed.length ?? 0) > 0;
        loadFeed(null, true, hasCachedItems).catch(err => {
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

                {!error && isExploreFallback && feed.length > 0 && (
                    <div className="explore-fallback-banner">
                        <div className="explore-fallback-header">
                            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round">
                                <circle cx="12" cy="12" r="10"/>
                                <polygon points="16.24 7.76 14.12 14.12 7.76 16.24 9.88 9.88 16.24 7.76"/>
                            </svg>
                            <div>
                                <h3>Gợi ý khám phá dành cho bạn</h3>
                                <p>Theo dõi các tài khoản yêu thích để cá nhân hóa bảng tin của bạn</p>
                            </div>
                        </div>
                    </div>
                )}

                {feed.map((post) => {
                    // Determine if this is a SHARE post or PHOTO post
                    const isShare = post.type === 'SHARE';
                    // For SHARE type, use original photo info
                    const displayPhotoId = isShare ? post.originalPhotoId : post.id;
                    const displayImageUrl = isShare ? post.originalImageUrl : post.imageUrl;
                    const displayCaption = isShare ? post.shareCaption : post.caption;
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
                                setFeed(prevFeed => {
                                    const nextFeed = prevFeed.map(p =>
                                        String(p.id) === String(photoId) || String(p.originalPhotoId) === String(photoId)
                                            ? {
                                                ...p,
                                                isLikedByCurrentUser: updatedPhoto.isLikedByCurrentUser ?? p.isLikedByCurrentUser,
                                                isSavedByCurrentUser: updatedPhoto.isSavedByCurrentUser ?? p.isSavedByCurrentUser,
                                                likeCount: updatedPhoto.likeCount ?? p.likeCount,
                                                originalLikeCount: p.type === 'SHARE' ? (updatedPhoto.likeCount ?? p.originalLikeCount) : p.originalLikeCount,
                                                commentCount: updatedPhoto.commentCount ?? p.commentCount,
                                                originalCommentCount: p.type === 'SHARE' ? (updatedPhoto.commentCount ?? p.originalCommentCount) : p.originalCommentCount,
                                                shareCount: updatedPhoto.shareCount ?? p.shareCount,
                                                originalShareCount: p.type === 'SHARE' ? (updatedPhoto.shareCount ?? p.originalShareCount) : p.originalShareCount,
                                              }
                                            : p
                                    );
                                    const currentCache = pageCache.getHomeFeed();
                                    if (currentCache) {
                                        pageCache.setHomeFeed({ ...currentCache, feed: nextFeed });
                                    }
                                    return nextFeed;
                                });
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

            {selectedPhotoId && (() => {
                const matchedPost = feed.find(
                    p => String(p.id) === String(selectedPhotoId) || String(p.originalPhotoId) === String(selectedPhotoId)
                );
                const initialPhoto = matchedPost
                    ? matchedPost.type === 'SHARE'
                        ? {
                            id: matchedPost.originalPhotoId,
                            userId: matchedPost.originalAuthorId || matchedPost.userId,
                            username: matchedPost.originalAuthorUsername || matchedPost.username,
                            userImageUrl: matchedPost.originalAuthorAvatar || matchedPost.userImageUrl,
                            imageUrl: matchedPost.originalImageUrl,
                            caption: matchedPost.originalPhotoCaption ?? matchedPost.shareCaption,
                            likeCount: matchedPost.originalLikeCount ?? 0,
                            commentCount: matchedPost.originalCommentCount ?? 0,
                            shareCount: matchedPost.originalShareCount ?? 0,
                            isLikedByCurrentUser: matchedPost.isLikedByCurrentUser ?? false,
                            isSavedByCurrentUser: matchedPost.isSavedByCurrentUser ?? false,
                            createdAt: matchedPost.createdAt,
                            tags: matchedPost.tags || []
                        }
                        : matchedPost
                    : null;

                return (
                    <PhotoModal
                        photoId={selectedPhotoId}
                        initialPhoto={initialPhoto}
                        onClose={handlePhotoClose}
                        onPhotoClick={handlePhotoSelect}
                        onPhotoUpdate={(photoId, updatedPhoto) => {
                            // Sync state between modal and feed
                            setFeed(prevFeed => {
                                const nextFeed = prevFeed.map(post => 
                                    String(post.id) === String(photoId) || String(post.originalPhotoId) === String(photoId)
                                        ? { 
                                            ...post, 
                                            isLikedByCurrentUser: updatedPhoto.isLikedByCurrentUser ?? post.isLikedByCurrentUser,
                                            isSavedByCurrentUser: updatedPhoto.isSavedByCurrentUser ?? post.isSavedByCurrentUser,
                                            likeCount: updatedPhoto.likeCount ?? post.likeCount,
                                            originalLikeCount: post.type === 'SHARE' ? (updatedPhoto.likeCount ?? post.originalLikeCount) : post.originalLikeCount,
                                            commentCount: updatedPhoto.commentCount ?? post.commentCount,
                                            originalCommentCount: post.type === 'SHARE' ? (updatedPhoto.commentCount ?? post.originalCommentCount) : post.originalCommentCount,
                                            shareCount: updatedPhoto.shareCount ?? post.shareCount,
                                            originalShareCount: post.type === 'SHARE' ? (updatedPhoto.shareCount ?? post.originalShareCount) : post.originalShareCount,
                                          }
                                        : post
                                );
                                const currentCache = pageCache.getHomeFeed();
                                if (currentCache) {
                                    pageCache.setHomeFeed({ ...currentCache, feed: nextFeed });
                                }
                                return nextFeed;
                            });
                        }}
                    />
                );
            })()} 
        </div>
    );
};

export default Home;
