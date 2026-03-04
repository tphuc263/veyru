import {NavLink} from 'react-router-dom'
import {useAuthContext} from '../../context/AuthContext'
import {useState, useEffect, useRef} from 'react'
import {Heart, Home, LogOut, Menu, MessageCircle, Moon, PlusSquare, Search, Sun, User} from 'lucide-react'

const SideBar = () => {
    const {user, logout, isAuthenticated} = useAuthContext()
    const [moreMenuOpen, setMoreMenuOpen] = useState(false)
    const [theme, setTheme] = useState(() => {
        return localStorage.getItem('theme') || 'dark'
    })
    const menuRef = useRef(null)

    // Apply theme on mount and change
    useEffect(() => {
        document.documentElement.setAttribute('data-theme', theme)
        localStorage.setItem('theme', theme)
    }, [theme])

    // Close menu when clicking outside
    useEffect(() => {
        const handleClickOutside = (e) => {
            if (menuRef.current && !menuRef.current.contains(e.target)) {
                setMoreMenuOpen(false)
            }
        }
        if (moreMenuOpen) {
            document.addEventListener('mousedown', handleClickOutside)
        }
        return () => document.removeEventListener('mousedown', handleClickOutside)
    }, [moreMenuOpen])

    // Navigation items for authenticated users
    const navItems = [
        {path: '/', label: 'Home', icon: <Home size={24}/>},
        {path: '/search', label: 'Search', icon: <Search size={24}/>},
        {path: '/messages', label: 'Messages', icon: <MessageCircle size={24}/>},
        {path: '/notifications', label: 'Notifications', icon: <Heart size={24}/>},
        {path: '/create', label: 'Create', icon: <PlusSquare size={24}/>},
        {path: '/profile', label: 'Profile', icon: <User size={24}/>},
    ]

    const handleLogout = () => {
        setMoreMenuOpen(false)
        logout()
    }

    const toggleTheme = () => {
        setTheme(prev => prev === 'dark' ? 'light' : 'dark')
    }

    if (!isAuthenticated) {
        return null
    }

    return (
        <aside className="sidebar">
            {/* Logo/Brand */}
            <div className="sidebar-brand">
                <h2>Share App</h2>
            </div>

            {/* Navigation Links */}
            <nav className="sidebar-nav">
                {navItems.map((item) => (
                    <NavLink
                        key={item.path}
                        to={item.path}
                        className={({isActive}) =>
                            `nav-item ${isActive ? 'active' : ''}`
                        }
                    >
                        <span className="nav-icon">{item.icon}</span>
                        <span className="nav-label">{item.label}</span>
                    </NavLink>
                ))}
            </nav>

            {/* More Menu (Instagram-style) */}
            <div className="sidebar-more-wrapper" ref={menuRef}>
                {moreMenuOpen && (
                    <div className="more-menu">
                        <button className="more-menu-item" onClick={toggleTheme}>
                            {theme === 'dark' ? <Sun size={20}/> : <Moon size={20}/>}
                            <span>Switch appearance</span>
                            <div className="theme-indicator">
                                <div className={`theme-toggle-track ${theme === 'dark' ? 'active' : ''}`}>
                                    <div className="theme-toggle-thumb"/>
                                </div>
                            </div>
                        </button>
                        <div className="more-menu-separator"/>
                        <button className="more-menu-item" onClick={handleLogout}>
                            <LogOut size={20}/>
                            <span>Log out</span>
                        </button>
                    </div>
                )}
                <button
                    className={`nav-item more-btn ${moreMenuOpen ? 'active' : ''}`}
                    onClick={() => setMoreMenuOpen(prev => !prev)}
                >
                    <span className="nav-icon"><Menu size={24}/></span>
                    <span className="nav-label">More</span>
                </button>
            </div>
        </aside>
    )
}

export default SideBar