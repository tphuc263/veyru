import {createContext, useContext, useEffect, useState} from 'react'
import {clearAuthData, getAuthData} from '../utils/storage'
import {useAuth as useAuthLogic} from '../hooks/useAuth'
import {toastSuccess} from '../utils/toastService'

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
        const initializeAuth = () => {
            try {
                const {token: savedToken, user: savedUser} = getAuthData()

                if (savedToken && savedUser) {
                    setToken(savedToken)
                    setUser(savedUser)
                }
            } catch {
                clearAuthData()
            } finally {
                setInitLoading(false);
            }
        }

        initializeAuth()
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