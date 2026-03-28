import { useEffect } from 'react';
import { useNavigate } from 'react-router-dom';

const OAuth2Redirect = () => {
    const navigate = useNavigate();

    useEffect(() => {
        const params = new URLSearchParams(window.location.search);
        const token = params.get('token');

        if (token) {
            localStorage.setItem('jwt', token);
            navigate('/home', { replace: true });
        } else {
            navigate('/login', { replace: true });
        }
    }, [navigate]);

    return null;
};

export default OAuth2Redirect;

