import { Client } from '@stomp/stompjs';
import { getToken } from '../utils/storage';

let client: Client | null = null;
let currentUserId: string | null = null;
let pendingMessages: any[] = [];

// Event listeners registry
const listeners = {
    connect: new Set<() => void>(),
    disconnect: new Set<(reason: string) => void>(),
    connect_error: new Set<(err: any) => void>(),
    user_online: new Set<(userId: string) => void>(),
    user_offline: new Set<(userId: string) => void>(),
    new_message: new Set<(message: any) => void>(),
    user_typing: new Set<(data: any) => void>(),
    user_stop_typing: new Set<(data: any) => void>(),
    messages_read: new Set<(data: any) => void>()
};

export const subscribeToSocketEvent = (event: keyof typeof listeners, callback: any) => {
    if (listeners[event]) {
        listeners[event].add(callback);
    }
};

export const unsubscribeFromSocketEvent = (event: keyof typeof listeners, callback: any) => {
    if (listeners[event]) {
        listeners[event].delete(callback);
    }
};

const triggerEvent = (event: keyof typeof listeners, data?: any) => {
    if (listeners[event]) {
        listeners[event].forEach(callback => callback(data));
    }
};

export const connectSocket = (userId: string): Client => {
    if (client && currentUserId === userId) {
        if (client.connected) {
            console.log('[socketService] Reusing existing STOMP client for user:', userId);
            return client;
        }
        console.log('[socketService] Reconnecting existing STOMP client for user:', userId);
        client.activate();
        return client;
    }

    if (client && currentUserId !== userId) {
        console.log('[socketService] Switching user from', currentUserId, 'to', userId);
        client.deactivate();
        client = null;
    }

    const token = getToken();
    currentUserId = userId;

    console.log('[socketService] Creating new STOMP connection for user:', userId);
    
    client = new Client({
        brokerURL: 'ws://localhost:8080/ws',
        connectHeaders: {
            Authorization: `Bearer ${token}`
        },
        reconnectDelay: 5000,
        heartbeatIncoming: 10000,
        heartbeatOutgoing: 10000,
        debug: (str) => {
            console.log('[STOMP]', str);
        }
    });

    client.onConnect = (frame) => {
        console.log('[socketService] STOMP connected:', frame);
        triggerEvent('connect');
        
        // Process offline queue
        if (pendingMessages.length > 0) {
            console.log(`[socketService] Processing ${pendingMessages.length} pending messages...`);
            pendingMessages.forEach(msg => {
                client?.publish({
                    destination: msg.destination,
                    body: msg.body
                });
            });
            pendingMessages = []; // clear after sending
        }

        // Subscribe to user messages
        client?.subscribe('/user/queue/messages', (message) => {
            if (message.body) {
                try {
                    const envelope = JSON.parse(message.body);
                    console.log('[socketService] Received message envelope:', envelope);
                    if (envelope.type === 'CHAT_MESSAGE') {
                        triggerEvent('new_message', envelope.payload);
                    }
                } catch (e) {
                    console.error('Failed to parse message', e);
                }
            }
        });

        // Subscribe to presence events
        client?.subscribe('/topic/presence', (message) => {
            if (message.body) {
                try {
                    const envelope = JSON.parse(message.body);
                    if (envelope.type === 'USER_ONLINE') {
                        triggerEvent('user_online', envelope.payload);
                    } else if (envelope.type === 'USER_OFFLINE') {
                        triggerEvent('user_offline', envelope.payload);
                    }
                } catch (e) {
                    console.error('Failed to parse presence message', e);
                }
            }
        });
    };

    client.onStompError = (frame) => {
        console.error('[socketService] STOMP error:', frame.headers['message']);
        triggerEvent('connect_error', new Error(frame.headers['message']));
    };

    client.onWebSocketError = (event) => {
        console.error('[socketService] STOMP websocket error:', event);
        triggerEvent('connect_error', event);
    };

    client.onWebSocketClose = (event) => {
        console.log('[socketService] STOMP disconnected:', event);
        triggerEvent('disconnect', 'closed');
    };

    client.activate();
    return client;
};

export const disconnectSocket = (): void => {
    if (client) {
        console.log('[socketService] Disconnecting STOMP for user:', currentUserId);
        client.deactivate();
        client = null;
        currentUserId = null;
    }
};

export const getSocket = (): Client | null => {
    return client;
};

export const publishEvent = (type: string, payload: any): void => {
    const envelope = {
        type,
        clientMessageId: crypto.randomUUID(),
        timestamp: new Date().toISOString(),
        payload
    };
    
    if (client?.connected) {
        client.publish({
            destination: '/app/chat.send',
            body: JSON.stringify(envelope)
        });
        console.log(`[Socket] Published event ${type}`);
    } else {
        console.warn(`[Socket] Client offline. Queueing event ${type}`);
        pendingMessages.push({
            destination: '/app/chat.send',
            body: JSON.stringify(envelope)
        });
    }
};

// Send a message via STOMP
export const sendSocketMessage = (receiverId: string, text: string): void => {
    publishEvent('CHAT_MESSAGE', { receiverId, text });
};

// Mark messages as read
export const markMessagesRead = (_conversationId: string, _otherUserId: string): void => {
    // publishEvent('MARK_READ', { conversationId: _conversationId, otherUserId: _otherUserId });
};

// Send typing indicator
export const sendTyping = (_receiverId: string): void => {
    // publishEvent('TYPING', { receiverId: _receiverId });
};

// Send stop typing indicator
export const sendStopTyping = (_receiverId: string): void => {
    // publishEvent('STOP_TYPING', { receiverId: _receiverId });
};
