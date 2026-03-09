// API Base URL - uses environment variable in production, falls back to localhost for development
export const API_BASE_URL: string = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api/v1';

// Default avatar SVG – grey circle with white person silhouette (Instagram/Facebook style)
export const DEFAULT_AVATAR = "data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 40 40'%3E%3Ccircle cx='20' cy='20' r='20' fill='%23c7c7c7'/%3E%3Cpath d='M20 22c-4.4 0-8 3.6-8 8h16c0-4.4-3.6-8-8-8zm0-2c2.2 0 4-1.8 4-4s-1.8-4-4-4-4 1.8-4 4 1.8 4 4 4z' fill='white'/%3E%3C/svg%3E";

// Socket URL - empty means use relative path through nginx proxy
export const SOCKET_URL: string = import.meta.env.VITE_SOCKET_URL || 'http://localhost:9092';

// OAuth URL - uses relative path in production (proxied through nginx)
export const OAUTH_URL: string = import.meta.env.VITE_OAUTH_URL || 'http://localhost:8080/oauth2/authorization/google';
