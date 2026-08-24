import { useState } from 'react'
import { Link } from 'react-router-dom'
import { forgotPassword } from '../../services/authService'
import { Loader } from '../../components/common/Loader.jsx'
import { toastSuccess, toastError } from '../../utils/toastService.js'
import '../../assets/styles/pages/authPage.css'

const ForgotPassword = () => {
    const [email, setEmail] = useState('')
    const [error, setError] = useState('')
    const [loading, setLoading] = useState(false)
    const [sent, setSent] = useState(false)

    const handleSubmit = async (e) => {
        e.preventDefault()
        setError('')

        if (!email.trim()) {
            setError('Email is required')
            return
        }

        if (!/\S+@\S+\.\S+/.test(email)) {
            setError('Please enter a valid email address')
            return
        }

        try {
            setLoading(true)
            await forgotPassword(email)
            setSent(true)
            toastSuccess.custom('Password reset email sent!')
        } catch (err) {
            const message = err?.response?.data?.message || err?.message || 'Failed to send reset email. Please try again.'
            setError(message)
            toastError.custom(message)
        } finally {
            setLoading(false)
        }
    }

    if (sent) {
        return (
            <div className="auth-container">
                <div className="auth-card">
                    <div className="auth-header">
                        <h1 className="instagram-title">Share App</h1>
                    </div>

                    <div className="forgot-password-success">
                        <div className="success-icon">
                            <svg width="64" height="64" viewBox="0 0 24 24" fill="none" stroke="#0095f6" strokeWidth="1.5">
                                <path d="M4 4h16c1.1 0 2 .9 2 2v12c0 1.1-.9 2-2 2H4c-1.1 0-2-.9-2-2V6c0-1.1.9-2 2-2z"/>
                                <polyline points="22,6 12,13 2,6"/>
                            </svg>
                        </div>
                        <h2>Check your email</h2>
                        <p>We've sent a password reset link to <strong>{email}</strong></p>
                        <p className="help-text">
                            The link will expire in 30 minutes. If you don't see the email, check your spam folder.
                        </p>
                        <button
                            className="submit-btn"
                            onClick={() => { setSent(false); setEmail(''); }}
                            style={{ marginTop: '8px' }}
                        >
                            Send again
                        </button>
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

    return (
        <div className="auth-container">
            <div className="auth-card">
                <div className="auth-header">
                    <h1 className="instagram-title">Share App</h1>
                </div>

                <div className="forgot-password-icon">
                    <svg width="64" height="64" viewBox="0 0 24 24" fill="none" stroke="var(--ig-secondary-text)" strokeWidth="1.5">
                        <rect x="3" y="11" width="18" height="11" rx="2" ry="2"/>
                        <path d="M7 11V7a5 5 0 0 1 10 0v4"/>
                        <circle cx="12" cy="16" r="1"/>
                    </svg>
                </div>

                <div className="forgot-password-text">
                    <h2>Trouble logging in?</h2>
                    <p>Enter your email address and we'll send you a link to reset your password.</p>
                </div>

                <form onSubmit={handleSubmit} className="auth-form" noValidate>
                    <div className="form-group">
                        <input
                            type="email"
                            name="email"
                            placeholder="Email address"
                            value={email}
                            onChange={(e) => {
                                setEmail(e.target.value)
                                if (error) setError('')
                            }}
                            className={`form-input ${error ? 'error' : ''}`}
                            disabled={loading}
                            autoComplete="email"
                            autoFocus
                            required
                        />
                        {error && (
                            <span className="error-message" role="alert">
                                {error}
                            </span>
                        )}
                    </div>

                    <button
                        type="submit"
                        disabled={loading || !email.trim()}
                        className="submit-btn"
                    >
                        {loading ? <Loader active={loading} /> : 'Send Reset Link'}
                    </button>
                </form>

                <div className="or-divider">
                    <span className="or-text">OR</span>
                </div>

                <div className="forgot-password-create">
                    <Link to="/register" className="auth-link create-account-link">
                        Create new account
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

export default ForgotPassword
