import {useState} from 'react'
import {Link, useNavigate} from 'react-router-dom'
import {useAuthContext} from '../../context/AuthContext'
import {Eye, EyeOff} from 'lucide-react';
import {validateRegistration} from "../../utils/helpers";
import {toastSuccess, toastError} from '../../utils/toastService.js';
import '../../assets/styles/pages/authPage.css'


const Register = () => {
    // Local form state
    const [formData, setFormData] = useState({
        username: '',
        email: '',
        password: '',
        confirmPassword: '',
    })

    // Local UI state
    const [errors, setErrors] = useState({})           // Form validation errors
    const [showPassword, setShowPassword] = useState(false)      // Password visibility
    const [showConfirmPassword, setShowConfirmPassword] = useState(false)  // Confirm password visibility

    // Get auth context and navigation
    const {register, operationLoading: loading} = useAuthContext()
    const navigate = useNavigate()

    /**
     * Handle input field changes
     * @param {Event} e - Input change event
     */
    const handleChange = (e) => {
        const {name, value} = e.target

        // Update form data
        setFormData(prev => ({
            ...prev,
            [name]: value
        }))

        // Clear error for this field when user starts typing
        if (errors[name]) {
            setErrors(prev => ({
                ...prev,
                [name]: ''
            }))
        }
    }

    /**
     * Validate form using business logic validation
     * @returns {boolean} True if form is valid
     */
    const validateForm = () => {
        const validation = validateRegistration(formData)

        if (!validation.isValid) {
            setErrors(validation.errors)
            return false
        }

        // Clear any existing errors
        setErrors({})
        return true
    }

    /**
     * Handle form submission
     * @param {Event} e - Form submit event
     */
    const handleSubmit = async (e) => {
        e.preventDefault()

        // Step 1: Validate form data
        if (!validateForm()) {
            return
        }

        try {
            // Step 2: Prepare registration data (remove confirmPassword)
            const {confirmPassword, ...registrationData} = formData

            // Step 3: Attempt registration using auth context
            const result = await register(registrationData)

            // Step 4: Handle registration result
            if (result.success) {
                // Registration successful - navigate to login with success message
                toastSuccess.registerSuccess();
                navigate('/login', {
                    replace: true,
                    state: {
                        message: 'Đăng ký thành công! Vui lòng đăng nhập.'
                    }
                })
            } else {
                // Registration failed - show error message
                toastError.registerFailed(result.error);
                setErrors({
                    submit: result.error || 'Đăng ký thất bại. Vui lòng thử lại.'
                })
            }
        } catch (error) {
            // Handle unexpected errors
            toastError.general();
            setErrors({
                submit: 'Đã có lỗi không mong muốn. Vui lòng thử lại.'
            })
        }
    }

    /**
     * Handle password visibility toggle
     */
    const togglePasswordVisibility = () => {
        setShowPassword(prev => !prev)
    }

    /**
     * Handle confirm password visibility toggle
     */
    const toggleConfirmPasswordVisibility = () => {
        setShowConfirmPassword(prev => !prev)
    }

    /**
     * Check password strength and return indicator
     * @param {string} password - Password to check
     * @returns {Object} Password strength info
     */
    const getPasswordStrength = (password) => {
        if (!password) return {strength: 'none', text: '', color: ''}

        if (password.length < 6) {
            return {strength: 'weak', text: 'Too short', color: '#ff4757'}
        } else if (password.length < 8) {
            return {strength: 'fair', text: 'Fair', color: '#ffa502'}
        } else if (!/(?=.*[a-z])(?=.*[A-Z])(?=.*\d)/.test(password)) {
            return {strength: 'good', text: 'Good', color: '#3742fa'}
        } else {
            return {strength: 'strong', text: 'Strong', color: '#2ed573'}
        }
    }

    const passwordStrength = getPasswordStrength(formData.password)

    return (
        <div className="auth-container">
            <div className="auth-card">
                {/* Header Section */}
                <div className="auth-header">
                    <h1>ShareApp</h1>
                </div>

                {/* Registration Form */}
                <form onSubmit={handleSubmit} className="auth-form" noValidate>

                    {/* Username Input Field */}
                    <div className="form-group">
                        <label htmlFor="username" className="form-label">
                            Username
                        </label>
                        <input
                            id="username"
                            type="text"
                            name="username"
                            placeholder="Choose a username"
                            value={formData.username}
                            onChange={handleChange}
                            className={`form-input ${errors.username ? 'error' : ''}`}
                            disabled={loading}
                            autoComplete="username"
                            required
                        />
                        {/* Username validation error */}
                        {errors.username && (
                            <span className="error-message" role="alert">
                                {errors.username}
                            </span>
                        )}
                    </div>

                    {/* Email Input Field */}
                    <div className="form-group">
                        <label htmlFor="email" className="form-label">
                            Email Address
                        </label>
                        <input
                            id="email"
                            type="email"
                            name="email"
                            placeholder="Enter your email"
                            value={formData.email}
                            onChange={handleChange}
                            className={`form-input ${errors.email ? 'error' : ''}`}
                            disabled={loading}
                            autoComplete="email"
                            required
                        />
                        {/* Email validation error */}
                        {errors.email && (
                            <span className="error-message" role="alert">
                                {errors.email}
                            </span>
                        )}
                    </div>

                    {/* Password Input Field */}
                    <div className="form-group">
                        <label htmlFor="password" className="form-label">
                            Password
                        </label>
                        <div className="password-input-wrapper">
                            <input
                                id="password"
                                type={showPassword ? 'text' : 'password'}
                                name="password"
                                placeholder="Create a password"
                                value={formData.password}
                                onChange={handleChange}
                                className={`form-input ${errors.password ? 'error' : ''}`}
                                disabled={loading}
                                autoComplete="new-password"
                                required
                            />
                            {/* Password visibility toggle */}
                            <button
                                type="button"
                                className="password-toggle"
                                onClick={togglePasswordVisibility}
                                disabled={loading}
                                aria-label={showPassword ? 'Hide password' : 'Show password'}
                            >
                                {showPassword ? <EyeOff className="w-5 h-5"/> : <Eye className="w-5 h-5"/>}
                            </button>
                        </div>

                        {/* Password validation error */}
                        {errors.password && (
                            <span className="error-message" role="alert">
                                {errors.password}
                            </span>
                        )}

                        {/* Password strength indicator */}
                        {!errors.password && formData.password && (
                            <div className="password-strength">
                                <span
                                    className="strength-text"
                                    style={{color: passwordStrength.color}}
                                >
                                    Password strength: {passwordStrength.text}
                                </span>
                                <div className="strength-bar">
                                    <div
                                        className={`strength-fill strength-${passwordStrength.strength}`}
                                        style={{backgroundColor: passwordStrength.color}}
                                    ></div>
                                </div>
                            </div>
                        )}
                    </div>

                    {/* Confirm Password Input Field */}
                    <div className="form-group">
                        <label htmlFor="confirmPassword" className="form-label">
                            Confirm Password
                        </label>
                        <div className="password-input-wrapper">
                            <input
                                id="confirmPassword"
                                type={showConfirmPassword ? 'text' : 'password'}
                                name="confirmPassword"
                                placeholder="Confirm your password"
                                value={formData.confirmPassword}
                                onChange={handleChange}
                                className={`form-input ${errors.confirmPassword ? 'error' : ''}`}
                                disabled={loading}
                                autoComplete="new-password"
                                required
                            />
                            {/* Confirm password visibility toggle */}
                            <button
                                type="button"
                                className="password-toggle"
                                onClick={toggleConfirmPasswordVisibility}
                                disabled={loading}
                                aria-label={showConfirmPassword ? 'Hide password' : 'Show password'}
                            >
                                {showPassword ? <EyeOff className="w-5 h-5"/> : <Eye className="w-5 h-5"/>}
                            </button>
                        </div>

                        {/* Confirm password validation error */}
                        {errors.confirmPassword && (
                            <span className="error-message" role="alert">
                                {errors.confirmPassword}
                            </span>
                        )}

                        {/* Password match indicator */}
                        {!errors.confirmPassword && formData.confirmPassword && formData.password && (
                            <span
                                className={`help-text ${
                                    formData.password === formData.confirmPassword
                                        ? 'success-text'
                                        : 'warning-text'
                                }`}
                            >
                                {formData.password === formData.confirmPassword
                                    ? '✓ Passwords match'
                                    : '⚠ Passwords do not match'
                                }
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
                        aria-describedby={loading ? 'loading-text' : undefined}
                    >
                        {loading ? (
                            <>
                                <span className="loading-spinner">⏳</span>
                                <span id="loading-text">Creating Account...</span>
                            </>
                        ) : (
                            'Create Account'
                        )}
                    </button>
                </form>

                {/* Footer Links */}
                <div className="auth-footer">
                    {/* Login link */}
                    <p>
                        Already have an account?{' '}
                        <Link
                            to="/login"
                            className="auth-link"
                            tabIndex={loading ? -1 : 0}
                        >
                            Sign in
                        </Link>
                    </p>

                    {/* Terms and privacy (placeholder) */}
                    <p className="terms-text">
                        By creating an account, you agree to our{' '}
                        <Link to="/terms" className="auth-link">Terms of Service</Link>
                        {' '}and{' '}
                        <Link to="/privacy" className="auth-link">Privacy Policy</Link>
                    </p>
                </div>
            </div>
        </div>
    )
}

export default Register