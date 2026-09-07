import api from '../config/ApiConfig';
import { clearCsrfToken, setCsrfToken } from '../config/CsrfToken';
import { LoginCredentials, RegisterData, AuthResponse } from '../types/api';
import type { components } from '../types/generated-api';

type AuthenticatedUserWire = components['schemas']['AuthenticatedUserResponse'];
type CsrfWire = components['schemas']['CsrfResponse'];

let csrfInitialization: Promise<void> | null = null;

const authenticatedUser = (wire: AuthenticatedUserWire): AuthResponse => {
    if (!wire.id || !wire.username || !wire.email || !wire.role) {
        throw new Error('Invalid authenticated-user response.');
    }
    return { id: wire.id, username: wire.username, email: wire.email, role: wire.role };
};

export const login = async (credentials: LoginCredentials): Promise<AuthResponse> => {
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

export const initializeCsrf = async (): Promise<void> => {
    csrfInitialization ??= api.get<CsrfWire>('/csrf')
        .then(response => {
            if (!response.data.token) {
                throw new Error('The backend returned an invalid CSRF response.');
            }
            setCsrfToken(response.data.token);
        })
        .finally(() => {
            csrfInitialization = null;
        });
    return csrfInitialization;
};

export const logout = async (): Promise<void> => {
    await api.delete('/sessions/current');
    clearCsrfToken();
    await initializeCsrf();
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
