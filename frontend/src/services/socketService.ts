import { Client } from '@stomp/stompjs';
import { markAsRead } from './messageService';

const resolveBrokerUrl = (): string => {
    const envSocketUrl = import.meta.env.VITE_SOCKET_URL;
    const configuredBase = typeof envSocketUrl === 'string' ? envSocketUrl.trim() : '';
    const base = configuredBase || (typeof window !== 'undefined' ? window.location.origin : 'http://localhost:8080');

    const normalizedBase = base.replace(/^http:\/\//, 'ws://').replace(/^https:\/\//, 'wss://').replace(/\/+$/, '');
    return normalizedBase.endsWith('/ws') ? normalizedBase : `${normalizedBase}/ws`;
};

const csrfToken = (): string =>
    document.cookie
        .split('; ')
        .find(cookie => cookie.startsWith('XSRF-TOKEN='))
        ?.slice('XSRF-TOKEN='.length) ?? '';

let client: Client | null = null;
let currentUserId: string | null = null;
interface PendingMessage {
    destination: string;
    body: string;
}

type SocketListener = (data: unknown) => void;

let pendingMessages: PendingMessage[] = [];

// Event listeners registry
const listeners = {
    connect: new Set<SocketListener>(),
    disconnect: new Set<SocketListener>(),
    connect_error: new Set<SocketListener>(),
    user_online: new Set<SocketListener>(),
    user_offline: new Set<SocketListener>(),
    new_message: new Set<SocketListener>(),
    user_typing: new Set<SocketListener>(),
    user_stop_typing: new Set<SocketListener>(),
    messages_read: new Set<SocketListener>()
};

export const subscribeToSocketEvent = (event: keyof typeof listeners, callback: SocketListener) => {
    if (listeners[event]) {
        listeners[event].add(callback);
    }
};

export const unsubscribeFromSocketEvent = (event: keyof typeof listeners, callback: SocketListener) => {
    if (listeners[event]) {
        listeners[event].delete(callback);
    }
};

const triggerEvent = (event: keyof typeof listeners, data?: unknown) => {
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

    currentUserId = userId;

    console.log('[socketService] Creating new STOMP connection for user:', userId);
    
    client = new Client({
        brokerURL: resolveBrokerUrl(),
        connectHeaders: {'X-XSRF-TOKEN': decodeURIComponent(csrfToken())},
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
                        triggerEvent('new_message', {
                            ...envelope.payload,
                            clientMessageId: envelope.clientMessageId,
                        });
                    } else if (envelope.type === 'MESSAGES_READ') {
                        triggerEvent('messages_read', envelope.payload);
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

export const publishEvent = (type: string, payload: unknown, clientMessageId: string = crypto.randomUUID()): string => {
    const envelope = {
        type,
        clientMessageId,
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
    return clientMessageId;
};

// Send a message via STOMP
export const sendSocketMessage = (conversationId: string, receiverId: string, text: string, clientMessageId: string): void => {
    publishEvent('CHAT_MESSAGE', { conversationId, receiverId, text }, clientMessageId);
};

export const isSocketConnected = (): boolean => {
    return Boolean(client?.connected);
};

// Mark messages as read via REST API
export const markMessagesRead = async (conversationId: string, _otherUserId: string): Promise<void> => {
    try {
        await markAsRead(conversationId);
        console.log(`[Socket] Marked messages as read for conversation: ${conversationId}`);
    } catch (e) {
        console.error('[Socket] Failed to mark messages as read:', e);
    }
};

// Send typing indicator
export const sendTyping = (_receiverId: string): void => {
    // publishEvent('TYPING', { receiverId: _receiverId });
};

// Send stop typing indicator
export const sendStopTyping = (_receiverId: string): void => {
    // publishEvent('STOP_TYPING', { receiverId: _receiverId });
};
