import axios from "axios";
import { API_BASE_URL } from "../utils/constants";
import { clearAuthData, getToken } from "../utils/storage";

const api = axios.create({
  baseURL: API_BASE_URL,
  timeout: 30000,
  headers: {
    "Content-Type": "application/json",
  },
}); 

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
  (error) => {
    if (error.response) {
      const { status, config } = error.response;
      if (
        status === 401 &&
        config.url !== "/auth/login" &&
        config.url !== "/auth/register"
      ) {
        clearAuthData();
        window.location.href =
          "/login?message=Your+session+has+expired.+Please+log+in+again.";
        return new Promise(() => {});
      }
    }
    return Promise.reject(error);
  }
);

export default api;
