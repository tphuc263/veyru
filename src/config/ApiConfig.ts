import axios, { AxiosError, InternalAxiosRequestConfig } from "axios";
import { API_BASE_URL } from "../utils/constants";

type RetryableRequest = InternalAxiosRequestConfig & { _retry?: boolean };

const noRefreshPaths = new Set(['/sessions', '/sessions/refresh', '/sessions/oauth2']);
let refreshPromise: Promise<void> | null = null;

const api = axios.create({
  baseURL: API_BASE_URL,
  timeout: 30000,
  withCredentials: true,
  withXSRFToken: true,
  xsrfCookieName: 'XSRF-TOKEN',
  xsrfHeaderName: 'X-XSRF-TOKEN',
  headers: {
    "Content-Type": "application/json",
  },
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
