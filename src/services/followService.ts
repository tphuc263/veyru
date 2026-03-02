import api from '../config/ApiConfig';

export interface FollowUser {
    id: string;
    userId: string;
    username: string;
    userImageUrl: string;
    firstName: string;
    lastName: string;
    bio: string;
    followedByCurrentUser: boolean;
}

export const follow = async (targetUserId: number): Promise<any> => {
    try {
        return await api.post(`/follows/follow/${targetUserId}`);
    } catch (e: any) {
        throw new Error(`Fail to follow: ${e.message}`);
    }
};

export const unfollow = async (targetUserId: number): Promise<any> => {
    try {
        return await api.post(`follows/unfollow/${targetUserId}`);
    } catch (e: any) {
        throw new Error(`Fail to unfollow: ${e.message}`);
    }
};

export const getFollowers = async (userId: string | number, page: number = 0, size: number = 20): Promise<FollowUser[]> => {
    try {
        const response = await api.get(`/follows/${userId}/followers`, {
            params: { page, size }
        });
        return response.data as FollowUser[];
    } catch (e: any) {
        throw new Error(`Failed to get followers: ${e.message}`);
    }
};

export const getFollowing = async (userId: string | number, page: number = 0, size: number = 20): Promise<FollowUser[]> => {
    try {
        const response = await api.get(`/follows/${userId}/following`, {
            params: { page, size }
        });
        return response.data as FollowUser[];
    } catch (e: any) {
        throw new Error(`Failed to get following: ${e.message}`);
    }
};
