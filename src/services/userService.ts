import api from "../config/ApiConfig";
import { User } from "../types/api";

export const getCurrentUserProfile = async (): Promise<User> => {
    try {
        const response = await api.get("/users/me");
        return response.data as User;
    } catch (e: any) {
        throw new Error(`Fail to get current user profile: ${e.message}`);
    }
};

export const getUserProfileById = async (userId: number): Promise<User> => {
    try {
        const response = await api.get(`/users/${userId}`);
        return response.data as User;
    } catch (e: any) {
        throw new Error(`Fail to get user profile by id: ${e.message}`);
    }
};

export const updateUserProfile = async (formData: FormData): Promise<User> => {
    try {
        const response = await api.put("/users/me", formData, {
            headers: {
                'Content-Type': 'multipart/form-data'
            }
        });
        return response.data as User;
    } catch (e: any) {
        throw new Error(`Fail to update user profile: ${e.message}`);
    }
};
