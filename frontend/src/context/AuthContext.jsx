import {createContext, useContext, useEffect, useState} from 'react'
import {useAuth as useAuthLogic} from '../hooks/useAuth'
import {toastSuccess} from '../utils/toastService'
import {currentSession, initializeCsrf} from '../services/authService'

const AuthContext = createContext(undefined)

// eslint-disable-next-line react-refresh/only-export-components
export const useAuthContext = () => {
    return useContext(AuthContext)
}

export const AuthProvider = ({children}) => {
    const [user, setUser] = useState(null)
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
                await initializeCsrf()
                setUser(await currentSession())
            } catch {
                setUser(null)
            } finally {
                setInitLoading(false);
            }
        }

        initializeAuth();
    }, [])

    useEffect(() => {
        const expire = () => setUser(null)
        window.addEventListener('auth:expired', expire)
        return () => window.removeEventListener('auth:expired', expire)
    }, [])

    const login = async (credentials) => {
        const result = await handleLogin(credentials)

        if (result.success) {
            setUser(result.data)
        }

        return result
    }

    const register = async (userData) => {
        const result = await handleRegister(userData)
        return result
    }

    const logout = async () => {
        const result = await handleLogout()
        setUser(null)
        toastSuccess.logoutSuccess()

        return result
    }

    const isAuthenticated = !!user

    const contextValue = {
      user,
      setUser,
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
