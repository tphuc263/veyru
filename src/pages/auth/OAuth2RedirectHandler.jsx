import { useEffect, useState } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { toastSuccess, toastError } from '../../utils/toastService';
import { Loader } from '../../components/common/Loader';
import { getCurrentUserProfile } from '../../services/userService';
import { setAuthData } from '../../utils/storage';
import '../../assets/styles/pages/authPage.css';

const OAuth2RedirectHandler = () => {
    const navigate = useNavigate();
    const [searchParams] = useSearchParams();
    const [status, setStatus] = useState('processing');

    useEffect(() => {
        const handleOAuth2Callback = async () => {
            const token = searchParams.get('token');
            const error = searchParams.get('error');

            if (token) {
                try {
                    // Bước 1: Lưu token tạm vào localStorage
                    localStorage.setItem('jwt', token);
                    
                    // Bước 2: Fetch thông tin user từ backend
                    const userProfile = await getCurrentUserProfile();
                    
                    // Bước 3: Lưu cả token và user info
                    setAuthData(token, {
                        id: userProfile.id,
                        username: userProfile.username,
                        email: userProfile.email,
                        role: userProfile.role
                    });
                    
                    setStatus('success');
                    toastSuccess.loginSuccess();
                    
                    // Redirect về home page và reload để AuthContext nhận user mới
                    setTimeout(() => {
                        window.location.href = '/home';
                    }, 1000);
                } catch (err) {
                    console.error('Failed to complete OAuth2 login:', err);
                    setStatus('error');
                    localStorage.removeItem('jwt');
                    toastError.loginFailed('Failed to retrieve user information');
                    setTimeout(() => {
                        navigate('/login', { replace: true });
                    }, 2000);
                }
            } else if (error) {
                setStatus('error');
                console.error('OAuth2 login failed:', error);
                toastError.loginFailed(decodeURIComponent(error));
                setTimeout(() => {
                    navigate('/login', { replace: true });
                }, 2000);
            } else {
                setStatus('error');
                toastError.loginFailed('Invalid OAuth2 response');
                setTimeout(() => {
                    navigate('/login', { replace: true });
                }, 2000);
            }
        };

        handleOAuth2Callback();
    }, [searchParams, navigate]);

    return (
        <div className="auth-container">
            <div className="auth-card">
                <div className="auth-header">
                    <h1 className="instagram-title">Share App</h1>
                </div>
                <div style={{ 
                    padding: '40px 20px', 
                    textAlign: 'center',
                    display: 'flex',
                    flexDirection: 'column',
                    alignItems: 'center',
                    gap: '20px'
                }}>
                    {status === 'processing' && (
                        <>
                            <Loader active={true} />
                            <p style={{ color: '#737373', fontSize: '14px' }}>
                                Processing your login...
                            </p>
                        </>
                    )}
                    {status === 'success' && (
                        <>
                            <div style={{ fontSize: '48px' }}>✅</div>
                            <p style={{ color: '#00c853', fontSize: '16px', fontWeight: '600' }}>
                                Login successful!
                            </p>
                            <p style={{ color: '#737373', fontSize: '14px' }}>
                                Redirecting to home...
                            </p>
                        </>
                    )}
                    {status === 'error' && (
                        <>
                            <div style={{ fontSize: '48px' }}>❌</div>
                            <p style={{ color: '#ed4956', fontSize: '16px', fontWeight: '600' }}>
                                Login failed
                            </p>
                            <p style={{ color: '#737373', fontSize: '14px' }}>
                                Redirecting to login page...
                            </p>
                        </>
                    )}
                </div>
            </div>
        </div>
    );
};

export default OAuth2RedirectHandler;
