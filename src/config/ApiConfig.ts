import axios from "axios";
import { API_BASE_URL } from "../utils/constants";
import { clearAuthData, getToken, setAuthData, getAuthData } from "../utils/storage";

const api = axios.create({
  baseURL: API_BASE_URL,
  timeout: 30000,
  headers: {
    "Content-Type": "application/json",
  },
  withCredentials: true, // Gửi cookie (refreshToken) trong mọi request
}); 

// State quản lý refresh token
let isRefreshing = false;
let failedQueue: Array<{
  resolve: (value?: unknown) => void;
  reject: (reason?: unknown) => void;
}> = [];

const processQueue = (error: unknown, token: string | null = null) => {
  failedQueue.forEach((prom) => {
    if (error) {
      prom.reject(error);
    } else {
      prom.resolve(token);
    }
  });
  failedQueue = [];
};

api.interceptors.request.use(
  (config) => {
    const token = getToken();
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

api.interceptors.response.use(
  (response) => {
    return response.data;
  },
  async (error) => {
    const originalRequest = error.config;

    // Nếu không phải lỗi 401 hoặc là request login/register thì reject luôn
    if (
      !error.response ||
      error.response.status !== 401 ||
      originalRequest.url === "/auth/login" ||
      originalRequest.url === "/auth/register" ||
      originalRequest.url === "/auth/refresh-token"
    ) {
      return Promise.reject(error);
    }

    // Tránh retry vô hạn
    if (originalRequest._retry) {
      clearAuthData();
      window.location.href =
        "/login?message=Your+session+has+expired.+Please+log+in+again.";
      return new Promise(() => {});
    }

    // Nếu đang refresh thì xếp hàng chờ
    if (isRefreshing) {
      return new Promise((resolve, reject) => {
        failedQueue.push({ resolve, reject });
      })
        .then((token) => {
          originalRequest.headers.Authorization = `Bearer ${token}`;
          return api(originalRequest);
        })
        .catch((err) => {
          return Promise.reject(err);
        });
    }

    originalRequest._retry = true;
    isRefreshing = true;

    try {
      // Gọi refresh-token endpoint — cookie httpOnly sẽ tự động được gửi nhờ withCredentials
      const response = await axios.post(
        `${API_BASE_URL}/auth/refresh-token`,
        {},
        { withCredentials: true }
      );

      const { jwt: newToken, id, username, email } = response.data.data;

      // Lưu token mới vào localStorage
      const currentUser = getAuthData().user;
      setAuthData(newToken, currentUser || { id, username, email });

      // Retry tất cả request đang chờ
      processQueue(null, newToken);

      // Retry request ban đầu
      originalRequest.headers.Authorization = `Bearer ${newToken}`;
      return api(originalRequest);
    } catch (refreshError) {
      processQueue(refreshError, null);
      clearAuthData();
      window.location.href =
        "/login?message=Your+session+has+expired.+Please+log+in+again.";
      return new Promise(() => {});
    } finally {
      isRefreshing = false;
    }
  }
);

export default api;
