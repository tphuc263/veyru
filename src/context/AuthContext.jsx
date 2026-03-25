import {createContext, useContext, useEffect, useState} from 'react'
import {clearAuthData, getAuthData, setAuthData} from '../utils/storage'
import {useAuth as useAuthLogic} from '../hooks/useAuth'
import {toastSuccess} from '../utils/toastService'
import api from '../config/ApiConfig'

const AuthContext = createContext(undefined)

// eslint-disable-next-line react-refresh/only-export-components
export const useAuthContext = () => {
    return useContext(AuthContext)
}

export const AuthProvider = ({children}) => {
    const [user, setUser] = useState(null)
    const [token, setToken] = useState(null)
    const [initLoading, setInitLoading] = useState(true);

    const {
        loading: operationLoading,
        handleLogin,
        handleRegister,
        handleLogout
    } = useAuthLogic()

    useEffect(() => {
        const initializeAuth = async () => {
            try {
                // 1. Check URL for OAuth2 token first
                const urlParams = new URLSearchParams(window.location.search);
                const oauthToken = urlParams.get('token');

                if (oauthToken) {
                    localStorage.setItem('jwt', oauthToken);
                    const userProfile = await api.get('/users/me');
                    setAuthData(oauthToken, {
                        id: userProfile.data.id,
                        username: userProfile.data.username,
                        email: userProfile.data.email,
                        role: userProfile.data.role
                    });
                    setToken(oauthToken);
                    setUser({
                        id: userProfile.data.id,
                        username: userProfile.data.username,
                        email: userProfile.data.email,
                        role: userProfile.data.role
                    });
                    // Clean URL
                    window.history.replaceState({}, document.title, window.location.pathname);
                    toastSuccess.loginSuccess();
                } else {
                    // 2. Fall back to localStorage
                    const {token: savedToken, user: savedUser} = getAuthData();
                    if (savedToken && savedUser) {
                        setToken(savedToken);
                        setUser(savedUser);
                    }
                }
            } catch (err) {
                console.error('Auth init failed:', err);
                clearAuthData();
            } finally {
                setInitLoading(false);
            }
        }

        initializeAuth();
    }, [])

    const login = async (credentials) => {
        const result = await handleLogin(credentials)

        if (result.success) {
            setToken(result.token)
            setUser(result.data)
        }

        return result
    }

    const register = async (userData) => {
        const result = await handleRegister(userData)
        return result
    }

    const logout = () => {
        const result = handleLogout()

        setToken(null)
        setUser(null)
        toastSuccess.logoutSuccess()

        return result
    }

    const isAuthenticated = !!(token && user)

    const contextValue = {
      user,
      setUser,
      token,
      loading: initLoading,
      operationLoading,
      isAuthenticated,
      login,
      register,
      logout,
    };

    return (
        <AuthContext.Provider value={contextValue}>
            {children}
        </AuthContext.Provider>
    )
}