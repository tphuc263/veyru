import { useState } from "react";
import axios from "axios";
import { login, logout, register } from "../services/authService";

interface LoginCredentials {
  identifier?: string;
  username?: string;
  password: string;
}

interface RegisterData {
  username: string;
  email: string;
  password: string;
  confirmPassword?: string;
}

export const useAuth = () => {
  const [loading, setLoading] = useState(false);

  const handleLogin = async (credentials: LoginCredentials) => {
    setLoading(true);

    try {
      const payload = {
        identifier: credentials.identifier || credentials.username || "",
        password: credentials.password
      };
      const response = await login(payload);
      return {
        success: true,
        data: response,
      };
    } catch (error: unknown) {
      const errorMessage = axios.isAxiosError(error) ? error.response?.data?.message : undefined;
      return {
        success: false,
        error: errorMessage || "Login failed",
        data: null,
      };
    } finally {
      setLoading(false);
    }
  };

  const handleRegister = async (userData: RegisterData) => {
    setLoading(true);
    try {
      const apiResponse = await register(userData);
      return {
        success: true,
        data: apiResponse,
        message: "Registration successful",
      };
    } catch (error: unknown) {
      const errorMessage = axios.isAxiosError(error) ? error.response?.data?.message : undefined;
      console.error("Registration operation failed:", error);
      return {
        success: false,
        error: errorMessage,
        data: null,
      };
    } finally {
      setLoading(false);
    }
  };

  const handleLogout = async () => {
    try {
      await logout();
      return { success: true, message: "Đăng xuất thành công" };
    } catch {
      return { success: false, message: "Lỗi khi đăng xuất" };
    }
  };

  return {
    loading,
    handleLogin,
    handleRegister,
    handleLogout,
  };
};
