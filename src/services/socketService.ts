import { io, Socket } from 'socket.io-client';
import { getToken } from '../utils/storage';
import { SOCKET_URL } from '../utils/constants';

let socket: Socket | null = null;
let currentUserId: string | null = null;

export const connectSocket = (userId: string): Socket => {
    // If already have a socket for the same user, return it
    if (socket && currentUserId === userId) {
        // If connected or connecting, return existing socket
        if (socket.connected || socket.io._readyState === 'opening') {
            console.log('[socketService] Reusing existing socket for user:', userId);
            return socket;
        }
        // If disconnected, reconnect
        console.log('[socketService] Reconnecting existing socket for user:', userId);
        socket.connect();
        return socket;
    }

    // If switching users, disconnect old socket first
    if (socket && currentUserId !== userId) {
        console.log('[socketService] Switching user from', currentUserId, 'to', userId);
        socket.disconnect();
        socket = null;
    }

    const token = getToken();
    currentUserId = userId;

    console.log('[socketService] Creating new socket connection for user:', userId);
    
    socket = io(SOCKET_URL, {
        query: {
            token,
            userId,
        },
        path: '/socket.io',
        transports: ['polling', 'websocket'],
        reconnection: true,
        reconnectionAttempts: 10,
        reconnectionDelay: 1000,
        reconnectionDelayMax: 5000,
        timeout: 20000,
        forceNew: false, // Reuse connection if possible
    });

    socket.on('connect', () => {
        console.log('[socketService] Socket.IO connected:', socket?.id);
    });

    socket.on('connect_error', (error) => {
        console.error('[socketService] Socket.IO connection error:', error.message);
    });

    socket.on('disconnect', (reason) => {
        console.log('[socketService] Socket.IO disconnected:', reason);
    });

    socket.on('reconnect', (attemptNumber) => {
        console.log('[socketService] Socket.IO reconnected after', attemptNumber, 'attempts');
    });

    socket.on('reconnect_attempt', (attemptNumber) => {
        console.log('[socketService] Socket.IO reconnection attempt:', attemptNumber);
    });

    return socket;
};

export const disconnectSocket = (): void => {
    if (socket) {
        console.log('[socketService] Disconnecting socket for user:', currentUserId);
        socket.disconnect();
        socket = null;
        currentUserId = null;
    }
};

export const getSocket = (): Socket | null => {
    return socket;
};

// Send a message via Socket.IO
export const sendSocketMessage = (receiverId: string, text: string): void => {
    console.log('[Socket] sendSocketMessage called:', { receiverId, text, connected: socket?.connected, socketId: socket?.id });
    if (socket?.connected) {
        socket.emit('send_message', { receiverId, text });
        console.log('[Socket] Emitted send_message event');
    } else {
        console.error('[Socket] Cannot send message - socket not connected!');
    }
};

// Mark messages as read
export const markMessagesRead = (conversationId: string, otherUserId: string): void => {
    if (socket?.connected) {
        socket.emit('mark_read', { conversationId, otherUserId });
    }
};

// Send typing indicator
export const sendTyping = (receiverId: string): void => {
    if (socket?.connected) {
        socket.emit('typing', { receiverId });
    }
};

// Send stop typing indicator
export const sendStopTyping = (receiverId: string): void => {
    if (socket?.connected) {
        socket.emit('stop_typing', { receiverId });
    }
};
