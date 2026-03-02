import { toast, ToastOptions } from 'react-toastify';

const toastConfig: ToastOptions = {
    position: "top-right",
    autoClose: 3000,
    hideProgressBar: false,
    closeOnClick: true,
    pauseOnHover: true,
    draggable: true,
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
    photoUploaded: (): void => showSuccess('Ảnh đã được đăng tải thành công!'),
    photoDeleted: (): void => showSuccess('Ảnh đã được xóa'),
    photoUpdated: (): void => showSuccess('Ảnh đã được cập nhật'),
    
    // Profile actions
    profileUpdated: (): void => showSuccess('Hồ sơ đã được cập nhật'),
    avatarUpdated: (): void => showSuccess('🖼️ Ảnh đại diện đã được thay đổi'),
    
    // Auth actions
    loginSuccess: (): void => showSuccess('Đăng nhập thành công!'),
    logoutSuccess: (): void => showSuccess('Đã đăng xuất'),
    registerSuccess: (): void => showSuccess('Đăng ký thành công! Vui lòng đăng nhập'),
    passwordResetSent: (): void => showSuccess('Email đặt lại mật khẩu đã được gửi!'),
    passwordResetSuccess: (): void => showSuccess('Đặt lại mật khẩu thành công!'),
    custom: (message: string): void => showSuccess(message),
    
    // Social actions
    followed: (): void => showSuccess('Đã theo dõi'),
    unfollowed: (): void => showSuccess('Đã hủy theo dõi'),
    liked: (): void => showSuccess('❤️ Đã thích'),
    unliked: (): void => showSuccess('Đã bỏ thích'),
    commented: (): void => showSuccess('💬 Đã bình luận'),
    commentDeleted: (): void => showSuccess('Đã xóa bình luận'),
} as const;

export const toastError = {
    // General errors
    general: (message?: string): void => showError(message || '❌ Có lỗi xảy ra, vui lòng thử lại'),
    network: (): void => showError('🌐 Lỗi kết nối mạng'),
    
    // Photo errors
    uploadFailed: (): void => showError('❌ Không thể đăng tải ảnh'),
    invalidImage: (): void => showError('⚠️ File không hợp lệ. Vui lòng chọn ảnh'),
    imageTooLarge: (): void => showError('⚠️ Ảnh quá lớn. Tối đa 10MB'),
    
    // Auth errors
    loginFailed: (message?: string): void => showError(message || '❌ Đăng nhập thất bại'),
    invalidCredentials: (): void => showError('⚠️ Email hoặc mật khẩu không đúng'),
    registerFailed: (message?: string): void => showError(message || '❌ Đăng ký thất bại'),
    
    // Profile errors
    updateFailed: (): void => showError('❌ Không thể cập nhật hồ sơ'),
    
    // Permission errors
    unauthorized: (): void => showError('🚫 Bạn không có quyền thực hiện thao tác này'),
    custom: (message: string): void => showError(message),
} as const;

export const toastInfo = {
    loading: (message: string = 'Đang xử lý...'): void => showInfo(message),
    copied: (): void => showInfo('📋 Đã sao chép'),
} as const;

export const toastWarning = {
    fillRequired: (): void => showWarning('⚠️ Vui lòng điền đầy đủ thông tin'),
    confirmAction: (): void => showWarning('⚠️ Vui lòng xác nhận'),
} as const;
