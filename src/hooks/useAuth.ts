import { useState } from "react";
import { login, register } from "../services/authService";
import { clearAuthData, setAuthData } from "../utils/storage";

interface LoginCredentials {
  username: string;
  password: string;
}

interface RegisterData {
  username: string;
  email: string;
  password: string;
}

export const useAuth = () => {
  const [loading, setLoading] = useState(false);

  const handleLogin = async (credentials: LoginCredentials) => {
    setLoading(true);

    try {
      const response = await login(credentials);
      const { jwt, id, username, email, role } = response;
      const userData = { id, username, email, role };
      setAuthData(jwt, userData);
      return {
        success: true,
        token: jwt,
        data: userData,
      };
    } catch (error: any) {
      const errorMessage = error?.response?.data?.message || "Login failed";
      return {
        success: false,
        error: errorMessage,
        data: null,
        token: null,
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
    } catch (error: any) {
      const errorMessage = error?.response?.data?.message || "Registration failed";
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

  const handleLogout = () => {
    try {
      clearAuthData();
      return { success: true, message: "Đăng xuất thành công" };
    } catch (error) {
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
