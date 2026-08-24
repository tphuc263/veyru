import api from "../config/ApiConfig";
import { User } from "../types/api";

type UserProfileResponse = User & { isFollowingByCurrentUser?: boolean };

const normalizeProfile = (profile: UserProfileResponse): User => ({
    ...profile,
    followingByCurrentUser: profile.isFollowingByCurrentUser
});

export const getCurrentUserProfile = async (): Promise<User> => {
    const response = await api.get<UserProfileResponse>("/users/me");
    return normalizeProfile(response.data);
};

export const getUserProfileById = async (userId: string | number): Promise<User> => {
    const response = await api.get<UserProfileResponse>(`/users/${userId}`);
    return normalizeProfile(response.data);
};

export const updateUserProfile = async (formData: FormData): Promise<User> => {
        const response = await api.patch<UserProfileResponse>("/users/me", formData, {
            headers: {
                'Content-Type': 'multipart/form-data'
            }
        });
        return normalizeProfile(response.data);
};
