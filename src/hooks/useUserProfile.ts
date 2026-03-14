import {useEffect, useState} from 'react';
import {useParams} from 'react-router-dom';
import {getCurrentUserProfile, getUserProfileById} from '../services/userService';
import {getUserPosts} from '../services/postService';

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

interface Photo {
    id: string | number;
    imageUrl: string;
    caption?: string;
    createdAt: string;
    userId?: string | number;
    username?: string;
    userImageUrl?: string;
    likeCount?: number;
    commentCount?: number;
    isLikedByCurrentUser?: boolean;
    tags?: string[];
}

interface PaginatedResponse<T> {
    content: T[];
    totalPages: number;
    totalElements: number;
    currentPage: number;
    size: number;
}

interface AsyncState<T> {
    data: T | null;
    loading: boolean;
    error: string | null;
}

interface PostsState extends AsyncState<Photo[]> {
    data: Photo[];
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

export const useUserProfile = (): UseUserProfileReturn => {
    const {userId} = useParams<{userId?: string}>();

    const [profile, setProfile] = useState<AsyncState<UserProfile>>({
        data: null,
        loading: true,
        error: null,
    });

    const [posts, setPosts] = useState<PostsState>({
        data: [],
        loading: false,
        error: null,
        currentPage: 0,
        totalPages: 0,
    });

    useEffect(() => {
        const fetchProfile = async (): Promise<void> => {
            setProfile({data: null, loading: true, error: null});
            try {
                const data: UserProfile = userId 
                    ? await getUserProfileById(userId) 
                    : await getCurrentUserProfile();
                setProfile({data, loading: false, error: null});
            } catch (error) {
                const errorMessage = error instanceof Error ? error.message : 'Unknown error occurred';
                setProfile({data: null, loading: false, error: errorMessage});
            }
        };

        fetchProfile();
    }, [userId]);

    useEffect(() => {
        const currUser = profile.data;
        if (!currUser) return;

        const fetchPosts = async (): Promise<void> => {
            setPosts(prev => ({...prev, loading: true, error: null}));
            try {
                const postResponse: PaginatedResponse<any> = await getUserPosts(
                    String(currUser.id),
                    posts.currentPage
                );
                setPosts(prev => ({
                    ...prev,
                    data: posts.currentPage === 0
                        ? postResponse.content
                        : [...prev.data, ...postResponse.content],
                    totalPages: postResponse.totalPages,
                    loading: false,
                }));
            } catch (err) {
                const errorMessage = err instanceof Error ? err.message : 'Unknown error occurred';
                setPosts(prev => ({...prev, loading: false, error: errorMessage}));
            }
        };

        fetchPosts();
    }, [profile.data, posts.currentPage]);

    const handleLoadMore = (): void => {
        if (posts.currentPage < posts.totalPages - 1) {
            setPosts(prev => ({...prev, currentPage: prev.currentPage + 1}));
        }
    };

    return {profile, posts, handleLoadMore, setProfile, setPosts};
};