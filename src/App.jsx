import {Route, Routes, Navigate} from 'react-router-dom'
import {AuthProvider} from './context/AuthContext'
import {SocketProvider} from './context/SocketContext'
import Layout from './components/layout/Layout'
import ProtectedRoute, {PublicRoute} from './utils/ProtectedRoute.jsx'
import { ToastContainer } from 'react-toastify'
import 'react-toastify/dist/ReactToastify.css'
import './assets/styles/toast.css'

import Login from './pages/auth/Login'
import Register from './pages/auth/Register'
import OAuth2Redirect from './pages/auth/OAuth2Redirect'
import ForgotPassword from './pages/auth/ForgotPassword'
import ResetPassword from './pages/auth/ResetPassword'
import Home from './pages/home/Home'
import Search from './pages/search/Search'
import Create from './pages/create/Create'
import Profile from './pages/profile/Profile'
import EditProfileForm from './pages/profile/EditProfileForm.jsx'
import Messages from './pages/messages/Messages'
import Notifications from './pages/notifications/Notifications'

const NotFoundPage = () => (
    <div className="page-placeholder">
        <h2>❌ Page Not Found</h2>
        <p>The page you're looking for doesn't exist.</p>
    </div>
)

function App() {
    return (
        <AuthProvider>
            <SocketProvider>
                <Layout>
                    <Routes>
                        <Route path="/" element={<Navigate to="/login" replace/>} />

                        {/* Open routes — accessible regardless of auth state */}
                        <Route path="/auth/oauth2/redirect" element={<OAuth2Redirect />} />
                        <Route path="/forgot-password" element={<ForgotPassword />} />
                        <Route path="/reset-password" element={<ResetPassword />} />

                        <Route element={<PublicRoute />}>
                            <Route path="/login" element={<Login />} />
                            <Route path="/register" element={<Register />} />
                        </Route>

                        <Route element={<ProtectedRoute />}>
                            <Route path="/home" element={<Home />} />
                            <Route path="/search" element={<Search />} />
                            <Route path="/messages" element={<Messages />} />
                            <Route path="/notifications" element={<Notifications />} />
                            <Route path="/create" element={<Create />} />
                            <Route path="/profile" element={<Profile />} />
                            <Route path="/profile/:userId" element={<Profile />} />
                            <Route path="/edit-profile" element={<EditProfileForm />} />
                        </Route>

                        <Route path="*" element={<NotFoundPage />} />
                    </Routes>
                </Layout>
                <ToastContainer 
                    position="top-right"
                    autoClose={3000}
                    hideProgressBar={true}
                    newestOnTop
                    closeOnClick
                    rtl={false}
                    pauseOnFocusLoss={false}
                    draggable={false}
                    pauseOnHover={false}
                    theme="light"
                />
            </SocketProvider>
        </AuthProvider>
    )
}

export default App