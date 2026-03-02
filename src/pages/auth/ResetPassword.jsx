import { useState, useEffect } from 'react'
import { Link, useSearchParams, useNavigate } from 'react-router-dom'
import { resetPassword, validateResetToken } from '../../services/authService'
import { Loader } from '../../components/common/Loader.jsx'
import { toastSuccess, toastError } from '../../utils/toastService.js'
import '../../assets/styles/pages/authPage.css'

const ResetPassword = () => {
    const [searchParams] = useSearchParams()
    const navigate = useNavigate()
    const token = searchParams.get('token')

    const [formData, setFormData] = useState({
        newPassword: '',
        confirmPassword: ''
    })
    const [errors, setErrors] = useState({})
    const [loading, setLoading] = useState(false)
    const [validating, setValidating] = useState(true)
    const [tokenValid, setTokenValid] = useState(false)
    const [resetComplete, setResetComplete] = useState(false)

    useEffect(() => {
        const checkToken = async () => {
            if (!token) {
                setTokenValid(false)
                setValidating(false)
                return
            }

            try {
                const isValid = await validateResetToken(token)
                setTokenValid(isValid)
            } catch {
                setTokenValid(false)
            } finally {
                setValidating(false)
            }
        }

        checkToken()
    }, [token])

    const handleChange = (e) => {
        const { name, value } = e.target
        setFormData(prev => ({ ...prev, [name]: value }))
        if (errors[name]) {
            setErrors(prev => ({ ...prev, [name]: '' }))
        }
    }

    const validate = () => {
        const newErrors = {}

        if (!formData.newPassword) {
            newErrors.newPassword = 'New password is required'
        } else if (formData.newPassword.length < 6) {
            newErrors.newPassword = 'Password must be at least 6 characters'
        }

        if (!formData.confirmPassword) {
            newErrors.confirmPassword = 'Please confirm your password'
        } else if (formData.newPassword !== formData.confirmPassword) {
            newErrors.confirmPassword = 'Passwords do not match'
        }

        setErrors(newErrors)
        return Object.keys(newErrors).length === 0
    }

    const handleSubmit = async (e) => {
        e.preventDefault()
        if (!validate()) return

        try {
            setLoading(true)
            await resetPassword(token, formData.newPassword, formData.confirmPassword)
            setResetComplete(true)
            toastSuccess.custom('Password reset successfully!')
        } catch (err) {
            const message = err?.response?.data?.message || err?.message || 'Failed to reset password. Please try again.'
            setErrors({ submit: message })
            toastError.custom(message)
        } finally {
            setLoading(false)
        }
    }

    // Loading state
    if (validating) {
        return (
            <div className="auth-container">
                <div className="auth-card">
                    <div className="auth-header">
                        <h1 className="instagram-title">Share App</h1>
                    </div>
                    <div className="reset-password-loading">
                        <Loader active={true} />
                        <p>Validating your reset link...</p>
                    </div>
                </div>
            </div>
        )
    }

    // Invalid/expired token
    if (!tokenValid) {
        return (
            <div className="auth-container">
                <div className="auth-card">
                    <div className="auth-header">
                        <h1 className="instagram-title">Share App</h1>
                    </div>
                    <div className="reset-password-invalid">
                        <div className="invalid-icon">
                            <svg width="64" height="64" viewBox="0 0 24 24" fill="none" stroke="var(--ig-error-color)" strokeWidth="1.5">
                                <circle cx="12" cy="12" r="10"/>
                                <line x1="15" y1="9" x2="9" y2="15"/>
                                <line x1="9" y1="9" x2="15" y2="15"/>
                            </svg>
                        </div>
                        <h2>Invalid or Expired Link</h2>
                        <p>This password reset link is no longer valid. Please request a new one.</p>
                        <Link to="/forgot-password" className="submit-btn" style={{ textDecoration: 'none', display: 'inline-block', textAlign: 'center' }}>
                            Request New Link
                        </Link>
                    </div>
                </div>
                <div className="auth-signup-card">
                    <p>
                        <Link to="/login" className="auth-link">Back to Login</Link>
                    </p>
                </div>
            </div>
        )
    }

    // Success state 
    if (resetComplete) {
        return (
            <div className="auth-container">
                <div className="auth-card">
                    <div className="auth-header">
                        <h1 className="instagram-title">Share App</h1>
                    </div>
                    <div className="reset-password-success">
                        <div className="success-icon">
                            <svg width="64" height="64" viewBox="0 0 24 24" fill="none" stroke="var(--ig-success-color)" strokeWidth="1.5">
                                <path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"/>
                                <polyline points="22 4 12 14.01 9 11.01"/>
                            </svg>
                        </div>
                        <h2>Password Reset Complete</h2>
                        <p>Your password has been reset successfully. You can now log in with your new password.</p>
                        <button
                            className="submit-btn"
                            onClick={() => navigate('/login', { state: { message: 'Password reset successful! Please log in with your new password.' } })}
                        >
                            Go to Login
                        </button>
                    </div>
                </div>
            </div>
        )
    }

    // Reset form
    return (
        <div className="auth-container">
            <div className="auth-card">
                <div className="auth-header">
                    <h1 className="instagram-title">Share App</h1>
                </div>

                <div className="reset-password-icon">
                    <svg width="64" height="64" viewBox="0 0 24 24" fill="none" stroke="var(--ig-link-color)" strokeWidth="1.5">
                        <path d="M21 2l-2 2m-7.61 7.61a5.5 5.5 0 1 1-7.778 7.778 5.5 5.5 0 0 1 7.777-7.777zm0 0L15.5 7.5m0 0l3 3L22 7l-3-3m-3.5 3.5L19 4"/>
                    </svg>
                </div>

                <div className="reset-password-text">
                    <h2>Create New Password</h2>
                    <p>Your new password must be at least 6 characters long.</p>
                </div>

                <form onSubmit={handleSubmit} className="auth-form" noValidate>
                    <div className="form-group">
                        <input
                            type="password"
                            name="newPassword"
                            placeholder="New password"
                            value={formData.newPassword}
                            onChange={handleChange}
                            className={`form-input ${errors.newPassword ? 'error' : ''}`}
                            disabled={loading}
                            autoComplete="new-password"
                            autoFocus
                            required
                        />
                        {errors.newPassword && (
                            <span className="error-message" role="alert">
                                {errors.newPassword}
                            </span>
                        )}
                    </div>

                    <div className="form-group">
                        <input
                            type="password"
                            name="confirmPassword"
                            placeholder="Confirm new password"
                            value={formData.confirmPassword}
                            onChange={handleChange}
                            className={`form-input ${errors.confirmPassword ? 'error' : ''}`}
                            disabled={loading}
                            autoComplete="new-password"
                            required
                        />
                        {errors.confirmPassword && (
                            <span className="error-message" role="alert">
                                {errors.confirmPassword}
                            </span>
                        )}
                    </div>

                    {errors.submit && (
                        <div className="error-message submit-error" role="alert">
                            {errors.submit}
                        </div>
                    )}

                    <button
                        type="submit"
                        disabled={loading}
                        className="submit-btn"
                    >
                        {loading ? <Loader active={loading} /> : 'Reset Password'}
                    </button>
                </form>
            </div>

            <div className="auth-signup-card">
                <p>
                    <Link to="/login" className="auth-link">Back to Login</Link>
                </p>
            </div>
        </div>
    )
}

export default ResetPassword
