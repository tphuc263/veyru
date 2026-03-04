import { io, Socket } from 'socket.io-client';
import { getToken } from '../utils/storage';

const SOCKET_URL = 'http://localhost:9092';

let socket: Socket | null = null;

export const connectSocket = (userId: string): Socket => {
    if (socket?.connected) {
        return socket;
    }

    const token = getToken();

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
    });

    socket.on('connect', () => {
        console.log('Socket.IO connected:', socket?.id);
    });

    socket.on('connect_error', (error) => {
        console.error('Socket.IO connection error:', error.message);
    });

    socket.on('disconnect', (reason) => {
        console.log('Socket.IO disconnected:', reason);
    });

    return socket;
};

export const disconnectSocket = (): void => {
    if (socket) {
        socket.disconnect();
        socket = null;
    }
};

export const getSocket = (): Socket | null => {
    return socket;
};

// Send a message via Socket.IO
export const sendSocketMessage = (receiverId: string, text: string): void => {
    if (socket?.connected) {
        socket.emit('send_message', { receiverId, text });
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
