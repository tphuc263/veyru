import {useAuthContext} from '../../context/AuthContext'
import SideBar from './SideBar'
import {Loader} from '../common/Loader.jsx'

const Layout = ({children}) => {
    const {isAuthenticated, loading} = useAuthContext()

    if (loading) {
        return <Loader />
    }

    if (isAuthenticated) {
        return (
            <div className="app-layout">
                <SideBar/>
                <main className="main-content">
                    {children}
                </main>
            </div>
        )
    }

    return (
        <div className="auth-layout">
            {children}
        </div>
    )
}

export default Layout