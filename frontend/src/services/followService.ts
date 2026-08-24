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

type FollowResponse = Omit<FollowUser, 'followedByCurrentUser'> & {
    isFollowedByCurrentUser: boolean;
};

const normalizeUsers = (users: FollowResponse[]): FollowUser[] =>
    users.map(user => ({
        ...user,
        followedByCurrentUser: user.isFollowedByCurrentUser
    }));

export const follow = async (targetUserId: string | number): Promise<void> => {
    await api.put(`/users/me/following/${targetUserId}`);
};

export const unfollow = async (targetUserId: string | number): Promise<void> => {
    await api.delete(`/users/me/following/${targetUserId}`);
};

export const getFollowers = async (userId: string | number, page: number = 0, size: number = 20): Promise<FollowUser[]> => {
        const response = await api.get<FollowResponse[]>(`/users/${userId}/followers`, {
            params: { page, size }
        });
        return normalizeUsers(response.data);
};

export const getFollowing = async (userId: string | number, page: number = 0, size: number = 20): Promise<FollowUser[]> => {
        const response = await api.get<FollowResponse[]>(`/users/${userId}/following`, {
            params: { page, size }
        });
        return normalizeUsers(response.data);
};

export const checkFollowStatus = async (followingId: string | number): Promise<boolean> => {
    const response = await api.get<boolean>(`/users/me/following/${followingId}`);
    return response.data;
};
