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

const NotificationsPage = () => (
    <div className="page-placeholder">
        <h2>Thông báo</h2>
        <p>Cập nhật thông báo mới nhất...</p>
    </div>
)

const SettingsPage = () => (
    <div className="page-placeholder">
        <h2>Cài đặt</h2>
    </div>
);

const HelpPage = () => (
    <div className="page-placeholder">
        <h2>❓ Help & Support</h2>
    </div>
);

const TermsPage = () => (
    <div className="page-placeholder">
        <h2>📄 Terms of Service</h2>
    </div>
);

const PrivacyPage = () => (
    <div className="page-placeholder">
        <h2>🔒 Privacy Policy</h2>
    </div>
);

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
                        <Route path="/terms" element={<TermsPage />} />
                        <Route path="/privacy" element={<PrivacyPage />} />

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
                            <Route path="/notifications" element={<NotificationsPage />} />
                            <Route path="/create" element={<Create />} />
                            <Route path="/profile" element={<Profile />} />
                            <Route path="/profile/:userId" element={<Profile />} />
                            <Route path="/edit-profile" element={<EditProfileForm />} />
                            <Route path="/settings" element={<SettingsPage />} />
                            <Route path="/help" element={<HelpPage />} />
                        </Route>

                        <Route path="*" element={<NotFoundPage />} />
                    </Routes>
                </Layout>
                <ToastContainer 
                    position="top-right"
                    autoClose={3000}
                    hideProgressBar={false}
                    newestOnTop
                    closeOnClick
                    rtl={false}
                    pauseOnFocusLoss
                    draggable
                    pauseOnHover
                    theme="light"
                />
            </SocketProvider>
        </AuthProvider>
    )
}

export default App