import { createContext, useContext, useEffect, useState, useCallback, useRef } from 'react';
import { useAuthContext } from './AuthContext';
import {
    connectSocket,
    disconnectSocket,
    getSocket,
} from '../services/socketService';

const SocketContext = createContext(undefined);

// eslint-disable-next-line react-refresh/only-export-components
export const useSocketContext = () => {
    return useContext(SocketContext);
};

export const SocketProvider = ({ children }) => {
    const { user, isAuthenticated } = useAuthContext();
    const [isConnected, setIsConnected] = useState(false);
    const [onlineUsers, setOnlineUsers] = useState(new Set());
    const socketRef = useRef(null);
    const messageListenersRef = useRef(new Set());
    const connectionAttemptedRef = useRef(false);

    // Initialize socket when user is authenticated
    useEffect(() => {
        if (!isAuthenticated || !user?.id) {
            // Disconnect if user logs out
            if (socketRef.current) {
                disconnectSocket();
                socketRef.current = null;
                setIsConnected(false);
                setOnlineUsers(new Set());
                connectionAttemptedRef.current = false;
            }
            return;
        }

        // Avoid duplicate connection attempts
        if (connectionAttemptedRef.current) {
            return;
        }
        connectionAttemptedRef.current = true;

        console.log('[SocketContext] Initializing global socket for user:', user.id);
        const socket = connectSocket(user.id);
        socketRef.current = socket;

        const onConnect = () => {
            console.log('[SocketContext] Socket connected globally:', socket.id);
            setIsConnected(true);
        };

        const onDisconnect = (reason) => {
            console.log('[SocketContext] Socket disconnected:', reason);
            setIsConnected(false);
            // Reset connection flag to allow reconnection
            if (reason === 'io server disconnect' || reason === 'transport close') {
                connectionAttemptedRef.current = false;
            }
        };

        const onConnectError = (error) => {
            console.error('[SocketContext] Connection error:', error.message);
            setIsConnected(false);
        };

        const onUserOnline = (userId) => {
            setOnlineUsers((prev) => new Set([...prev, userId]));
        };

        const onUserOffline = (userId) => {
            setOnlineUsers((prev) => {
                const next = new Set(prev);
                next.delete(userId);
                return next;
            });
        };

        // Global message handler that forwards to all registered listeners
        const onNewMessage = (message) => {
            console.log('[SocketContext] Received new_message:', message);
            messageListenersRef.current.forEach((listener) => {
                try {
                    listener(message);
                } catch (err) {
                    console.error('[SocketContext] Error in message listener:', err);
                }
            });
        };

        // Set up listeners
        socket.on('connect', onConnect);
        socket.on('disconnect', onDisconnect);
        socket.on('connect_error', onConnectError);
        socket.on('user_online', onUserOnline);
        socket.on('user_offline', onUserOffline);
        socket.on('new_message', onNewMessage);

        // Check if already connected (in case of reconnection)
        if (socket.connected) {
            setIsConnected(true);
        }

        return () => {
            // Only cleanup listeners, DON'T disconnect here
            // We want to keep the connection alive while user is authenticated
            socket.off('connect', onConnect);
            socket.off('disconnect', onDisconnect);
            socket.off('connect_error', onConnectError);
            socket.off('user_online', onUserOnline);
            socket.off('user_offline', onUserOffline);
            socket.off('new_message', onNewMessage);
        };
    }, [isAuthenticated, user?.id]);

    // Subscribe to new messages - called by Messages component
    const subscribeToMessages = useCallback((listener) => {
        messageListenersRef.current.add(listener);
        console.log('[SocketContext] Message listener added, total:', messageListenersRef.current.size);
        
        return () => {
            messageListenersRef.current.delete(listener);
            console.log('[SocketContext] Message listener removed, total:', messageListenersRef.current.size);
        };
    }, []);

    // Check if a specific user is online
    const isUserOnline = useCallback((userId) => {
        return onlineUsers.has(userId);
    }, [onlineUsers]);

    // Force reconnect (useful for debugging or manual retry)
    const reconnect = useCallback(() => {
        if (user?.id) {
            connectionAttemptedRef.current = false;
            disconnectSocket();
            socketRef.current = null;
            setIsConnected(false);
            
            // Small delay before reconnecting
            setTimeout(() => {
                const socket = connectSocket(user.id);
                socketRef.current = socket;
            }, 100);
        }
    }, [user?.id]);

    const contextValue = {
        socket: socketRef.current,
        isConnected,
        onlineUsers,
        isUserOnline,
        subscribeToMessages,
        reconnect,
    };

    return (
        <SocketContext.Provider value={contextValue}>
            {children}
        </SocketContext.Provider>
    );
};

export default SocketContext;
