import api from '../config/ApiConfig';
import { LoginCredentials, RegisterData, AuthResponse } from '../types/api';

export const login = async (credentials: LoginCredentials): Promise<AuthResponse> => {
    const response = await api.post('/auth/login', credentials);
    return response.data as AuthResponse;
};

export const register = async (userData: RegisterData): Promise<AuthResponse> => {
    const response = await api.post('/auth/register', userData);
    return response.data as AuthResponse;
};

export const forgotPassword = async (email: string): Promise<any> => {
    const response = await api.post('/auth/forgot-password', { email });
    return response;
};

export const resetPassword = async (token: string, newPassword: string, confirmPassword: string): Promise<any> => {
    const response = await api.post('/auth/reset-password', { token, newPassword, confirmPassword });
    return response;
};

export const validateResetToken = async (token: string): Promise<boolean> => {
    const response = await api.get('/auth/validate-reset-token', { params: { token } });
    return (response as any).data as boolean;
};
