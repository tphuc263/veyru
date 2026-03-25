import {useState} from 'react'
import {Link, useLocation, useNavigate} from 'react-router-dom'
import {useAuthContext} from '../../context/AuthContext'
import {validateCredentials} from "../../utils/helpers";
import {Loader} from "../../components/common/Loader.jsx";
import {toastSuccess, toastError} from '../../utils/toastService.js';
import {OAUTH_URL} from '../../utils/constants';
import '../../assets/styles/pages/authPage.css'


const Login = () => {
    const [formData, setFormData] = useState({
        identifier: '',
        password: '',
    })

    const [errors, setErrors] = useState({})

    const { login, operationLoading: loading } = useAuthContext();
    const navigate = useNavigate()
    const location = useLocation()

    const from = location.state?.from?.pathname || '/'

    const handleChange = (e) => {
        const {name, value} = e.target

        setFormData(prev => ({
            ...prev,
            [name]: value
        }))

        if (errors[name]) {
            setErrors(prev => ({
                ...prev,
                [name]: ''
            }))
        }
    }

    const handleSubmit = async (e) => {
        e.preventDefault()

        const validationResult = validateCredentials(formData)
        setErrors(validationResult.errors)

        if (!validationResult.isValid) {
            return
        }

        try {
            const result = await login(formData)

            if (result.success) {
                toastSuccess.loginSuccess();
                navigate(from, {replace: true})
            } else {
                toastError.loginFailed(result.error);
                setErrors({
                    submit: result?.error || 'Đăng nhập thất bại. Vui lòng thử lại.'
                })
            }
        } catch (error) {
            toastError.general();
            setErrors({
                submit: 'Đã xảy ra lỗi không mong muốn. Vui lòng thử lại.'
            })
        }
    }

    return (
        <div className="auth-container">
            <div className="auth-card">
                {/* Header Section */}
                <div className="auth-header">
                    <h1 className="instagram-title">Share App</h1>
                    {location.state?.message && (
                        <div className="success-message">
                            {location.state.message}
                        </div>
                    )}
                </div>

                {/* Login Form */}
                <form onSubmit={handleSubmit} className="auth-form" noValidate>

                    {/* Identifier Input Field */}
                    <div className="form-group">
                        <input
                            id="identifier"
                            type="text"
                            name="identifier"
                            placeholder="Phone number, username, or email"
                            value={formData.identifier}
                            onChange={handleChange}
                            className={`form-input ${errors.identifier ? 'error' : ''}`}
                            disabled={loading}
                            autoComplete="username"
                            required
                        />
                        {/* Show identifier validation error */}
                        {errors.identifier && (
                            <span className="error-message" role="alert">
                                {errors.identifier}
                            </span>
                        )}
                    </div>

                    {/* Password Input Field */}
                    <div className="form-group">
                        <input
                            id="password"
                            type="password"
                            name="password"
                            placeholder="Password"
                            value={formData.password}
                            onChange={handleChange}
                            className={`form-input ${errors.password ? 'error' : ''}`}
                            disabled={loading}
                            autoComplete="current-password"
                            required
                        />
                        {/* Show password validation error */}
                        {errors.password && (
                            <span className="error-message" role="alert">
                                {errors.password}
                            </span>
                        )}
                    </div>

                    {/* Form submission error */}
                    {errors.submit && (
                        <div className="error-message submit-error" role="alert">
                            {errors.submit}
                        </div>
                    )}

                    {/* Submit Button */}
                    <button
                        type="submit"
                        disabled={loading}
                        className="submit-btn"
                    >
                        {loading ? (
                            <Loader active={loading} />
                        ) : (
                            'Log in'
                        )}
                    </button>
                </form>

                {/* OR Divider */}
                <div className="or-divider">
                    <span className="or-text">OR</span>
                </div>

                {/* Google Login Button */}
                <button
                    type="button"
                    className="google-login-btn"
                    disabled={loading}
                    onClick={() => {
                        window.location.href = OAUTH_URL;
                    }}
                >
                    <svg className="google-icon" viewBox="0 0 24 24" width="18" height="18">
                        <path fill="#4285F4" d="M22.56 12.25c0-.78-.07-1.53-.2-2.25H12v4.26h5.92c-.26 1.37-1.04 2.53-2.21 3.31v2.77h3.57c2.08-1.92 3.28-4.74 3.28-8.09z"/>
                        <path fill="#34A853" d="M12 23c2.97 0 5.46-.98 7.28-2.66l-3.57-2.77c-.98.66-2.23 1.06-3.71 1.06-2.86 0-5.29-1.93-6.16-4.53H2.18v2.84C3.99 20.53 7.7 23 12 23z"/>
                        <path fill="#FBBC05" d="M5.84 14.09c-.22-.66-.35-1.36-.35-2.09s.13-1.43.35-2.09V7.07H2.18C1.43 8.55 1 10.22 1 12s.43 3.45 1.18 4.93l2.85-2.22.81-.62z"/>
                        <path fill="#EA4335" d="M12 5.38c1.62 0 3.06.56 4.21 1.64l3.15-3.15C17.45 2.09 14.97 1 12 1 7.7 1 3.99 3.47 2.18 7.07l3.66 2.84c.87-2.6 3.3-4.53 6.16-4.53z"/>
                    </svg>
                    Log in with Google
                </button>

                {/* Forgot Password Link */}
                <div className="forgot-password-link">
                    <Link
                        to="/forgot-password"
                        className="auth-link"
                        tabIndex={loading ? -1 : 0}
                    >
                        Forgot password?
                    </Link>
                </div>

            </div>

            {/* Sign Up Card */}
            <div className="auth-signup-card">
                <p>
                    Don't have an account?{' '}
                    <Link
                        to="/register"
                        className="auth-link"
                        tabIndex={loading ? -1 : 0}
                    >
                        Sign up
                    </Link>
                </p>
            </div>
        </div>
    )
}

export default Login