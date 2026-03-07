import {NavLink} from 'react-router-dom'
import {useAuthContext} from '../../context/AuthContext'
import {useState, useEffect, useRef} from 'react'
import {Heart, Home, LogOut, Menu, MessageCircle, Moon, PlusSquare, Search, Sun, User} from 'lucide-react'
import NotificationDropdown from '../features/NotificationDropdown'
import { getUnreadCount } from '../../services/notificationService'

const SideBar = () => {
    const {user, logout, isAuthenticated} = useAuthContext()
    const [moreMenuOpen, setMoreMenuOpen] = useState(false)
    const [notificationOpen, setNotificationOpen] = useState(false)
    const [unreadNotifications, setUnreadNotifications] = useState(0)
    const [theme, setTheme] = useState(() => {
        return localStorage.getItem('theme') || 'dark'
    })
    const menuRef = useRef(null)
    const notificationRef = useRef(null)

    // Fetch unread notification count
    useEffect(() => {
        if (!isAuthenticated) return;
        
        const fetchUnread = async () => {
            try {
                const response = await getUnreadCount();
                setUnreadNotifications(response.data ?? response ?? 0);
            } catch (error) {
                console.error('Failed to fetch unread count:', error);
            }
        };
        
        fetchUnread();
        const interval = setInterval(fetchUnread, 30000);
        return () => clearInterval(interval);
    }, [isAuthenticated]);

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
            if (notificationRef.current && !notificationRef.current.contains(e.target)) {
                setNotificationOpen(false)
            }
        }
        if (moreMenuOpen || notificationOpen) {
            document.addEventListener('mousedown', handleClickOutside)
        }
        return () => document.removeEventListener('mousedown', handleClickOutside)
    }, [moreMenuOpen, notificationOpen])

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

            {/* Navigation Links - Instagram order: Home, Search, Messages, Notifications, Create, Profile */}
            <nav className="sidebar-nav">
                {/* Home */}
                <NavLink to="/home" className={({isActive}) => `nav-item ${isActive ? 'active' : ''}`}>
                    <span className="nav-icon"><Home size={24}/></span>
                    <span className="nav-label">Home</span>
                </NavLink>
                
                {/* Search */}
                <NavLink to="/search" className={({isActive}) => `nav-item ${isActive ? 'active' : ''}`}>
                    <span className="nav-icon"><Search size={24}/></span>
                    <span className="nav-label">Search</span>
                </NavLink>
                
                {/* Messages */}
                <NavLink to="/messages" className={({isActive}) => `nav-item ${isActive ? 'active' : ''}`}>
                    <span className="nav-icon"><MessageCircle size={24}/></span>
                    <span className="nav-label">Messages</span>
                </NavLink>
                
                {/* Notifications with Dropdown */}
                <div className="notification-nav-wrapper" ref={notificationRef}>
                    <button
                        className={`nav-item ${notificationOpen ? 'active' : ''}`}
                        onClick={() => setNotificationOpen(prev => !prev)}
                    >
                        <span className="nav-icon">
                            <Heart size={24}/>
                            {unreadNotifications > 0 && (
                                <span className="notification-badge-sidebar">
                                    {unreadNotifications > 99 ? '99+' : unreadNotifications}
                                </span>
                            )}
                        </span>
                        <span className="nav-label">Notifications</span>
                    </button>
                    {notificationOpen && (
                        <NotificationDropdown 
                          isControlled={true} 
                          onClose={() => setNotificationOpen(false)} 
                        />
                    )}
                </div>
                
                {/* Create */}
                <NavLink to="/create" className={({isActive}) => `nav-item ${isActive ? 'active' : ''}`}>
                    <span className="nav-icon"><PlusSquare size={24}/></span>
                    <span className="nav-label">Create</span>
                </NavLink>
                
                {/* Profile */}
                <NavLink to="/profile" className={({isActive}) => `nav-item ${isActive ? 'active' : ''}`}>
                    <span className="nav-icon"><User size={24}/></span>
                    <span className="nav-label">Profile</span>
                </NavLink>
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