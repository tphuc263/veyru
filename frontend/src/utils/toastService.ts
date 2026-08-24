import { toast, ToastOptions } from 'react-toastify';

const toastConfig: ToastOptions = {
    position: "top-right",
    autoClose: 3000,
    hideProgressBar: true,
    closeOnClick: true,
    pauseOnHover: false,
    pauseOnFocusLoss: false,
    draggable: false,
};

type ToastType = 'success' | 'error' | 'info' | 'warning' | 'default';

// Generic toast function
export const showToast = (type: ToastType, message: string): void => {
    switch (type) {
        case 'success':
            toast.success(message, toastConfig);
            break;
        case 'error':
            toast.error(message, toastConfig);
            break;
        case 'info':
            toast.info(message, toastConfig);
            break;
        case 'warning':
            toast.warning(message, toastConfig);
            break;
        default:
            toast(message, toastConfig);
    }
};

export const showSuccess = (message: string): void => {
    toast.success(message, toastConfig);
};

export const showError = (message: string): void => {
    toast.error(message, toastConfig);
};

export const showInfo = (message: string): void => {
    toast.info(message, toastConfig);
};

export const showWarning = (message: string): void => {
    toast.warning(message, toastConfig);
};

// Toast cho các action cụ thể
export const toastSuccess = {
    // Photo actions
    photoUploaded: (): void => showSuccess('Ảnh đã đăng'),
    photoDeleted: (): void => showSuccess('Đã xóa ảnh'),
    photoUpdated: (): void => showSuccess('Đã cập nhật ảnh'),

    // Profile actions
    profileUpdated: (): void => showSuccess('Đã cập nhật hồ sơ'),
    avatarUpdated: (): void => showSuccess('Đã đổi ảnh đại diện'),

    // Auth actions
    loginSuccess: (): void => showSuccess('Đã đăng nhập'),
    logoutSuccess: (): void => showSuccess('Đã đăng xuất'),
    registerSuccess: (): void => showSuccess('Đã tạo tài khoản'),
    passwordResetSent: (): void => showSuccess('Đã gửi email'),
    passwordResetSuccess: (): void => showSuccess('Đã đặt lại mật khẩu'),
    custom: (message: string): void => showSuccess(message),

    // Social actions
    followed: (): void => showSuccess('Đã theo dõi'),
    unfollowed: (): void => showSuccess('Đã hủy theo dõi'),
    liked: (): void => showSuccess('Đã thích'),
    unliked: (): void => showSuccess('Đã bỏ thích'),
    commented: (): void => showSuccess('Đã bình luận'),
    commentDeleted: (): void => showSuccess('Đã xóa bình luận'),
} as const;

export const toastError = {
    // General errors
    general: (message?: string): void => showError(message || 'Đã xảy ra lỗi'),
    network: (): void => showError('Lỗi kết nối'),

    // Photo errors
    uploadFailed: (): void => showError('Không thể đăng ảnh'),
    invalidImage: (): void => showError('File không hợp lệ'),
    imageTooLarge: (): void => showError('Ảnh quá lớn (tối đa 10MB)'),

    // Auth errors
    loginFailed: (message?: string): void => showError(message || 'Đăng nhập thất bại'),
    invalidCredentials: (): void => showError('Sai email hoặc mật khẩu'),
    registerFailed: (message?: string): void => showError(message || 'Đăng ký thất bại'),

    // Profile errors
    updateFailed: (): void => showError('Không thể cập nhật'),

    // Permission errors
    unauthorized: (): void => showError('Không có quyền thực hiện'),
    custom: (message: string): void => showError(message),
} as const;

export const toastInfo = {
    loading: (message: string = 'Đang tải...'): void => showInfo(message),
    copied: (): void => showInfo('Đã sao chép'),
} as const;

export const toastWarning = {
    fillRequired: (): void => showWarning('Vui lòng điền đầy đủ'),
    confirmAction: (): void => showWarning('Vui lòng xác nhận'),
} as const;
