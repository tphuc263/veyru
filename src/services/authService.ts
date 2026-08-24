import api from '../config/ApiConfig';
import { LoginCredentials, RegisterData, AuthResponse } from '../types/api';
import type { components } from '../types/generated-api';

type AuthenticatedUserWire = components['schemas']['AuthenticatedUserResponse'];

const authenticatedUser = (wire: AuthenticatedUserWire): AuthResponse => {
    if (!wire.id || !wire.username || !wire.email || !wire.role) {
        throw new Error('Invalid authenticated-user response.');
    }
    return { id: wire.id, username: wire.username, email: wire.email, role: wire.role };
};

export const login = async (credentials: LoginCredentials): Promise<AuthResponse> => {
    const response = await api.post<AuthenticatedUserWire>('/sessions', credentials);
    return authenticatedUser(response.data);
};

export const currentSession = async (): Promise<AuthResponse> => {
    const response = await api.get<AuthenticatedUserWire>('/sessions/current');
    return authenticatedUser(response.data);
};

export const exchangeOAuthCode = async (code: string): Promise<AuthResponse> => {
    const response = await api.post<AuthenticatedUserWire>('/sessions/oauth2', { code });
    return authenticatedUser(response.data);
};

export const initializeCsrf = async (): Promise<void> => {
    await api.get('/csrf');
};

export const logout = async (): Promise<void> => {
    await api.delete('/sessions/current');
};

export const register = async (userData: RegisterData): Promise<void> => {
    const { confirmPassword: _, ...request } = userData;
    await api.post('/users', request);
};

export const forgotPassword = async (email: string): Promise<void> => {
    await api.post('/password-reset-requests', { email });
};

export const resetPassword = async (token: string, newPassword: string, confirmPassword: string): Promise<void> => {
    await api.post('/password-resets', { token, newPassword, confirmPassword });
};
