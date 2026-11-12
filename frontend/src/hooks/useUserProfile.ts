import {useEffect, useState, useRef} from 'react';
import {useParams} from 'react-router-dom';
import {getCurrentUserProfile, getUserProfileById} from '../services/userService';
import {getUserPosts} from '../services/postService';
import {pageCache} from '../utils/pageCache';
import type {UnifiedPost} from '../types/api';

interface UserProfile {
    id: string | number;
    username: string;
    email?: string;
    fullName?: string;
    bio?: string;
    imageUrl?: string;
    stats?: {
        posts?: number;
        followers?: number;
        following?: number;
    };
    followingByCurrentUser?: boolean;
}

interface PaginatedResponse<T> {
    items?: T[];
    content?: T[];
    totalPages: number;
    totalElements: number;
    page?: number;
    currentPage?: number;
    size: number;
}

interface AsyncState<T> {
    data: T | null;
    loading: boolean;
    error: string | null;
}

interface PostsState extends AsyncState<UnifiedPost[]> {
    data: UnifiedPost[];
    currentPage: number;
    totalPages: number;
}

interface UseUserProfileReturn {
    profile: AsyncState<UserProfile>;
    posts: PostsState;
    handleLoadMore: () => void;
    setProfile: React.Dispatch<React.SetStateAction<AsyncState<UserProfile>>>;
    setPosts: React.Dispatch<React.SetStateAction<PostsState>>;
}

export const useUserProfile = (currentUserId?: string | number): UseUserProfileReturn => {
    const {userId} = useParams<{userId?: string}>();

    const cacheKey = userId ? String(userId) : currentUserId ? String(currentUserId) : 'me';
    const initialCached = pageCache.getProfile(cacheKey);

    const [profile, setProfile] = useState<AsyncState<UserProfile>>(() => ({
        data: (initialCached?.profile as unknown as UserProfile) ?? null,
        loading: !initialCached?.profile,
        error: null,
    }));

    const [posts, setPosts] = useState<PostsState>(() => ({
        data: initialCached?.posts ?? [],
        loading: false,
        error: null,
        currentPage: initialCached?.currentPage ?? 0,
        totalPages: initialCached?.totalPages ?? 0,
    }));

    const activeCacheKeyRef = useRef(cacheKey);

    useEffect(() => {
        activeCacheKeyRef.current = cacheKey;
        const cached = pageCache.getProfile(cacheKey);

        if (cached) {
            setProfile({
                data: cached.profile as unknown as UserProfile,
                loading: false,
                error: null,
            });
            setPosts({
                data: cached.posts,
                loading: false,
                error: null,
                currentPage: cached.currentPage,
                totalPages: cached.totalPages,
            });
            if (pageCache.isProfileFresh(cacheKey)) {
                return;
            }
        } else {
            setProfile({data: null, loading: true, error: null});
            setPosts({data: [], loading: true, error: null, currentPage: 0, totalPages: 0});
        }

        let isMounted = true;

        const fetchProfileAndInitialPosts = async (): Promise<void> => {
            try {
                const knownTargetUserId = userId ? String(userId) : currentUserId ? String(currentUserId) : null;

                if (knownTargetUserId) {
                    // Parallel fetch: profile + first page of posts simultaneously (eliminates waterfall!)
                    const [profileData, postResponse] = await Promise.all([
                        userId ? getUserProfileById(userId) : getCurrentUserProfile(),
                        getUserPosts(knownTargetUserId, 0),
                    ]);

                    if (!isMounted) return;

                    const postItems = postResponse.items || postResponse.content || [];
                    setProfile({data: profileData as UserProfile, loading: false, error: null});
                    setPosts({
                        data: postItems,
                        loading: false,
                        error: null,
                        currentPage: 0,
                        totalPages: postResponse.totalPages,
                    });

                    pageCache.setProfile(String(profileData.id || cacheKey), {
                        profile: profileData as unknown as Record<string, unknown>,
                        posts: postItems,
                        currentPage: 0,
                        totalPages: postResponse.totalPages,
                    });
                    if (cacheKey !== String(profileData.id)) {
                        pageCache.setProfile(cacheKey, {
                            profile: profileData as unknown as Record<string, unknown>,
                            posts: postItems,
                            currentPage: 0,
                            totalPages: postResponse.totalPages,
                        });
                    }
                } else {
                    const profileData: UserProfile = userId
                        ? await getUserProfileById(userId)
                        : await getCurrentUserProfile();

                    if (!isMounted) return;
                    setProfile({data: profileData, loading: false, error: null});

                    const postResponse: PaginatedResponse<UnifiedPost> = await getUserPosts(
                        String(profileData.id),
                        0
                    );
                    if (!isMounted) return;
                    const postItems = postResponse.items || postResponse.content || [];
                    setPosts({
                        data: postItems,
                        loading: false,
                        error: null,
                        currentPage: 0,
                        totalPages: postResponse.totalPages,
                    });

                    pageCache.setProfile(cacheKey, {
                        profile: profileData as unknown as Record<string, unknown>,
                        posts: postItems,
                        currentPage: 0,
                        totalPages: postResponse.totalPages,
                    });
                }
            } catch (error) {
                if (!isMounted) return;
                const errorMessage = error instanceof Error ? error.message : 'Unknown error occurred';
                if (!cached) {
                    setProfile({data: null, loading: false, error: errorMessage});
                    setPosts(prev => ({...prev, loading: false, error: errorMessage}));
                }
            }
        };

        void fetchProfileAndInitialPosts();

        return () => {
            isMounted = false;
        };
    }, [userId, currentUserId, cacheKey]);

    const handleLoadMore = (): void => {
        const currUser = profile.data;
        if (!currUser || posts.loading || posts.currentPage >= posts.totalPages - 1) return;

        const nextPage = posts.currentPage + 1;
        setPosts(prev => ({...prev, loading: true, error: null}));

        getUserPosts(String(currUser.id), nextPage)
            .then((postResponse: PaginatedResponse<UnifiedPost>) => {
                const postItems = postResponse.items || postResponse.content || [];
                setPosts(prev => {
                    const nextData = [...prev.data, ...postItems];
                    pageCache.setProfile(cacheKey, {
                        profile: currUser as unknown as Record<string, unknown>,
                        posts: nextData,
                        currentPage: nextPage,
                        totalPages: postResponse.totalPages,
                    });
                    return {
                        ...prev,
                        data: nextData,
                        currentPage: nextPage,
                        totalPages: postResponse.totalPages,
                        loading: false,
                    };
                });
            })
            .catch(err => {
                const errorMessage = err instanceof Error ? err.message : 'Unknown error occurred';
                setPosts(prev => ({...prev, loading: false, error: errorMessage}));
            });
    };

    return {profile, posts, handleLoadMore, setProfile, setPosts};
};
