import { LoginCredentials, RegisterData, ValidationResult, ValidationErrors } from '../types/api';

/**
 * Debounce function calls
 * @param func - Function to debounce
 * @param wait - Wait time in milliseconds
 * @returns Debounced function
 */
export const debounce = <T extends (...args: any[]) => any>(
    func: T,
    wait: number
): ((...args: Parameters<T>) => void) => {
    let timeout: number | undefined;
    
    return function executedFunction(...args: Parameters<T>) {
        const later = () => {
            timeout = undefined;
            func(...args);
        };
        
        if (timeout !== undefined) {
            clearTimeout(timeout);
        }
        timeout = window.setTimeout(later, wait);
    }; 
};

export const validateCredentials = (credentials: LoginCredentials): ValidationResult => {
    const errors: ValidationErrors = {};

    if (!credentials.identifier?.trim()) {
        errors.identifier = 'Email, username, or phone number is required';
    }
    // No strict format validation since it can be email, username, or phone

    if (!credentials.password) {
        errors.password = 'Password is required';
    } else if (credentials.password.length < 6) {
        errors.password = 'Password must be at least 6 characters long';
    }

    return {
        isValid: Object.keys(errors).length === 0,
        errors
    };
};

export const validateRegistration = (userData: RegisterData): ValidationResult => {
    const errors: ValidationErrors = {};

    if (!userData.username?.trim()) {
        errors.username = 'Username is required';
    } else if (userData.username.length < 3) {
        errors.username = 'Username must be at least 3 characters long';
    } else if (!/^[a-zA-Z0-9._ ]+$/.test(userData.username)) {
        errors.username = 'Username can only contain letters, numbers, dots, underscores and white space';
    }

    if (!userData.email?.trim()) {
        errors.email = 'Email is required';
    } else if (!/\S+@\S+\.\S+/.test(userData.email)) {
        errors.email = 'Please enter a valid email address';
    }

    if (!userData.password) {
        errors.password = 'Password is required';
    } else if (userData.password.length < 6) {
        errors.password = 'Password must be at least 6 characters long';
    }

    if (!userData.confirmPassword) {
        errors.confirmPassword = 'Please confirm your password';
    } else if (userData.password !== userData.confirmPassword) {
        errors.confirmPassword = 'Passwords do not match';
    }

    return {
        isValid: Object.keys(errors).length === 0,
        errors
    };
};
