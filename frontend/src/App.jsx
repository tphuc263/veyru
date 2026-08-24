import {lazy, Suspense} from 'react'
import {Route, Routes, Navigate} from 'react-router-dom'
import {AuthProvider} from './context/AuthContext'
import {SocketProvider} from './context/SocketContext'
import Layout from './components/layout/Layout'
import ProtectedRoute, {PublicRoute} from './utils/ProtectedRoute.jsx'
import { ToastContainer } from 'react-toastify'
import 'react-toastify/dist/ReactToastify.css'
import './assets/styles/toast.css'

const Login = lazy(() => import('./pages/auth/Login'))
const Register = lazy(() => import('./pages/auth/Register'))
const OAuth2Redirect = lazy(() => import('./pages/auth/OAuth2Redirect'))
const ForgotPassword = lazy(() => import('./pages/auth/ForgotPassword'))
const ResetPassword = lazy(() => import('./pages/auth/ResetPassword'))
const Home = lazy(() => import('./pages/home/Home'))
const Search = lazy(() => import('./pages/search/Search'))
const Create = lazy(() => import('./pages/create/Create'))
const Profile = lazy(() => import('./pages/profile/Profile'))
const EditProfileForm = lazy(() => import('./pages/profile/EditProfileForm.jsx'))
const Messages = lazy(() => import('./pages/messages/Messages'))
const Notifications = lazy(() => import('./pages/notifications/Notifications'))

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
                    <Suspense fallback={<div className="page-placeholder">Loading…</div>}>
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
                    </Suspense>
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
