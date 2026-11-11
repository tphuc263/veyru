import axios, { AxiosError, InternalAxiosRequestConfig } from "axios";
import { API_BASE_URL } from "../utils/constants";
import { getCsrfToken, setCsrfToken } from './CsrfToken';
import type { components } from '../types/generated-api';

type CsrfWire = components['schemas']['CsrfResponse'];
type RetryableRequest = InternalAxiosRequestConfig & {
  _retry?: boolean;
  _csrfRetry?: boolean;
};

const noRefreshPaths = new Set(['/sessions', '/sessions/refresh', '/sessions/oauth2', '/csrf']);
const safeMethods = new Set(['get', 'head', 'options', 'trace']);
let refreshPromise: Promise<void> | null = null;
let csrfRefreshPromise: Promise<void> | null = null;

const api = axios.create({
  baseURL: API_BASE_URL,
  timeout: 30000,
  withCredentials: true,
  withXSRFToken: false,
  headers: {
    "Content-Type": "application/json",
  },
});

export const syncCsrfToken = async (): Promise<void> => {
  csrfRefreshPromise ??= api.get<CsrfWire>('/csrf')
    .then(response => {
      if (!response.data?.token) {
        throw new Error('The backend returned an invalid CSRF response.');
      }
      setCsrfToken(response.data.token);
    })
    .finally(() => {
      csrfRefreshPromise = null;
    });
  return csrfRefreshPromise;
};

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
  async (error: AxiosError<{ code?: string }>) => {
    const request = error.config as RetryableRequest | undefined;
    const path = request?.url?.split('?')[0];
    const method = request?.method?.toLowerCase() ?? 'get';
    const status = error.response?.status;
    const errorCode = error.response?.data?.code;

    if (
      status === 403 &&
      request &&
      !request._csrfRetry &&
      !safeMethods.has(method) &&
      path !== '/csrf' &&
      errorCode !== 'ACCESS_DENIED'
    ) {
      request._csrfRetry = true;
      await syncCsrfToken();
      return api(request);
    }

    if (status !== 401 || !request || request._retry || (path && noRefreshPaths.has(path))) {
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
