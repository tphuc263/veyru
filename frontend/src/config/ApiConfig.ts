import axios, { AxiosError, InternalAxiosRequestConfig } from "axios";
import { API_BASE_URL } from "../utils/constants";
import { getCsrfToken } from './CsrfToken';

type RetryableRequest = InternalAxiosRequestConfig & { _retry?: boolean };

const noRefreshPaths = new Set(['/sessions', '/sessions/refresh', '/sessions/oauth2']);
const safeMethods = new Set(['get', 'head', 'options', 'trace']);
let refreshPromise: Promise<void> | null = null;

const api = axios.create({
  baseURL: API_BASE_URL,
  timeout: 30000,
  withCredentials: true,
  withXSRFToken: false,
  headers: {
    "Content-Type": "application/json",
  },
});

api.interceptors.request.use(config => {
  const method = config.method?.toLowerCase() ?? 'get';
  const token = getCsrfToken();
  if (token && !safeMethods.has(method)) {
    config.headers.set('X-XSRF-TOKEN', token);
  }
  return config;
});

api.interceptors.response.use(
  response => response,
  async (error: AxiosError) => {
    const request = error.config as RetryableRequest | undefined;
    const path = request?.url?.split('?')[0];
    if (error.response?.status !== 401 || !request || request._retry || (path && noRefreshPaths.has(path))) {
      return Promise.reject(error);
    }

    request._retry = true;
    refreshPromise ??= api.post<void>('/sessions/refresh').then(() => undefined);
    try {
      await refreshPromise;
      return api(request);
    } catch (refreshError) {
      window.dispatchEvent(new Event('auth:expired'));
      return Promise.reject(refreshError);
    } finally {
      refreshPromise = null;
    }
  }
);

export default api;
