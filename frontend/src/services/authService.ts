import api, { syncCsrfToken } from '../config/ApiConfig';
import { clearCsrfToken, getCsrfToken } from '../config/CsrfToken';
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
    if (!getCsrfToken()) {
        await initializeCsrf();
    }
    const response = await api.post<AuthenticatedUserWire>('/sessions', credentials);
    await initializeCsrf();
    return authenticatedUser(response.data);
};

export const currentSession = async (): Promise<AuthResponse> => {
    const response = await api.get<AuthenticatedUserWire>('/sessions/current');
    return authenticatedUser(response.data);
};

export const exchangeOAuthCode = async (code: string): Promise<AuthResponse> => {
    await initializeCsrf();
    const response = await api.post<AuthenticatedUserWire>('/sessions/oauth2', { code });
    await initializeCsrf();
    return authenticatedUser(response.data);
};

export const initializeCsrf = async (): Promise<void> => syncCsrfToken();

export const logout = async (): Promise<void> => {
    await api.delete('/sessions/current');
    clearCsrfToken();
    await initializeCsrf();
};

export const register = async (userData: RegisterData): Promise<void> => {
    if (!getCsrfToken()) {
        await initializeCsrf();
    }
    const { confirmPassword: _, ...request } = userData;
    await api.post('/users', request);
};

export const forgotPassword = async (email: string): Promise<void> => {
    if (!getCsrfToken()) {
        await initializeCsrf();
    }
    await api.post('/password-reset-requests', { email });
};

export const resetPassword = async (token: string, newPassword: string, confirmPassword: string): Promise<void> => {
    if (!getCsrfToken()) {
        await initializeCsrf();
    }
    await api.post('/password-resets', { token, newPassword, confirmPassword });
};
