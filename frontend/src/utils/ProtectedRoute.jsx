import {Navigate, useLocation, Outlet} from 'react-router-dom'
import {useAuthContext} from '../context/AuthContext.jsx'
import {Loader} from "../components/common/Loader.jsx";

const ProtectedRoute = () => {
    const {isAuthenticated, loading} = useAuthContext()
    const location = useLocation()

    if (loading) {
        return <Loader />;
    }

    return isAuthenticated
        ? <Outlet />
        : <Navigate to="/login" state={{ from: location }} replace />;
}

export const PublicRoute = () => {
    const {isAuthenticated} = useAuthContext()


    return isAuthenticated
        ? <Navigate to="/home" replace />
        : <Outlet />;
}

export default ProtectedRoute