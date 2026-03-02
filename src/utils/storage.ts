const AUTH_TOKEN_KEY = 'jwt';
const AUTH_USER_KEY = 'auth_user';

interface StoredUser {
    id: number;
    username: string;
    email: string;
    role?: string;
}

interface AuthData {
    token: string | null;
    user: StoredUser | null;
}

export function setAuthData(token: string, user: StoredUser): void {
    try {
        localStorage.setItem(AUTH_TOKEN_KEY, token);
        localStorage.setItem(AUTH_USER_KEY, JSON.stringify(user));
    } catch (error) {
        console.error('Failed to store auth data:', error);
        throw new Error('Failed to save login data');
    }
}

export function getAuthData(): AuthData {
    try {
        const token = localStorage.getItem(AUTH_TOKEN_KEY);
        const userJson = localStorage.getItem(AUTH_USER_KEY);
        const user = userJson ? JSON.parse(userJson) : null;
        return { token, user };
    } catch (error) {
        console.error('Failed to retrieve auth data:', error);
        clearAuthData();
        return { token: null, user: null };
    }
}

export function clearAuthData(): void {
    try {
        localStorage.removeItem(AUTH_TOKEN_KEY);
        localStorage.removeItem(AUTH_USER_KEY);
    } catch (error) {
        console.error('Failed to clear auth data:', error);
    }
}

export function getToken(): string | null {
    const { token } = getAuthData();
    return token;
}
