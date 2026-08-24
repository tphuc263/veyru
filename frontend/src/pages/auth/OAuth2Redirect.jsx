import { useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuthContext } from '../../context/AuthContext';
import { exchangeOAuthCode } from '../../services/authService';

const OAuth2Redirect = () => {
    const navigate = useNavigate();
    const { setUser } = useAuthContext();

    useEffect(() => {
        const params = new URLSearchParams(window.location.search);
        const code = params.get('code');

        if (!code) {
            navigate('/login', { replace: true });
            return;
        }

        exchangeOAuthCode(code)
            .then(user => {
                setUser(user);
                navigate('/home', { replace: true });
            })
            .catch(() => navigate('/login?error=true', { replace: true }));
    }, [navigate, setUser]);

    return null;
};

export default OAuth2Redirect;
