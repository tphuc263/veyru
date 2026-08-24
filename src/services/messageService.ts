import api from '../config/ApiConfig';
import type { components } from '../types/generated-api';

type ConversationWire = components['schemas']['ConversationResponse'];
type MessageWire = components['schemas']['MessageResponse'];
type MessagePageWire = components['schemas']['PageResponseMessageResponse'];

export interface ConversationDTO {
    id: string;
    participantId: string;
    participantUsername: string;
    participantImageUrl?: string;
    lastMessageText?: string;
    lastMessageSenderId?: string;
    lastMessageAt?: string;
    unreadCount: number;
}

export interface MessageDTO {
    id: string;
    conversationId: string;
    senderId: string;
    receiverId: string;
    text: string;
    read: boolean;
    createdAt: string;
}

export interface PageResponse<T> {
    items: T[];
    totalElements: number;
    totalPages: number;
    page: number;
    size: number;
}

const conversation = (wire: ConversationWire): ConversationDTO => {
    if (!wire.id || !wire.participantId || !wire.participantUsername || wire.unreadCount === undefined) {
        throw new Error('Invalid conversation response.');
    }
    return { ...wire, id: wire.id, participantId: wire.participantId, participantUsername: wire.participantUsername, unreadCount: wire.unreadCount };
};

const message = (wire: MessageWire): MessageDTO => {
    if (!wire.id || !wire.conversationId || !wire.senderId || !wire.receiverId || wire.text === undefined || wire.read === undefined || !wire.createdAt) {
        throw new Error('Invalid message response.');
    }
    return { id: wire.id, conversationId: wire.conversationId, senderId: wire.senderId, receiverId: wire.receiverId, text: wire.text, read: wire.read, createdAt: wire.createdAt };
};

export const getConversations = async (): Promise<ConversationDTO[]> => {
    const response = await api.get<ConversationWire[]>('/conversations');
    return response.data.map(conversation);
};

export const getMessages = async (
    conversationId: string,
    page: number = 0,
    size: number = 50
): Promise<PageResponse<MessageDTO>> => {
    const response = await api.get<MessagePageWire>(`/conversations/${conversationId}/messages`, {
        params: { page, size },
    });
    const wire = response.data;
    if (!wire.items || wire.page === undefined || wire.size === undefined || wire.totalElements === undefined || wire.totalPages === undefined) {
        throw new Error('Invalid message page response.');
    }
    return { ...wire, items: wire.items.map(message), page: wire.page, size: wire.size, totalElements: wire.totalElements, totalPages: wire.totalPages };
};

export const sendMessage = async (receiverId: string, text: string, conversationId: string): Promise<MessageDTO> => {
    const response = await api.post<MessageWire>(`/conversations/${conversationId}/messages`, { conversationId, receiverId, text });
    return message(response.data);
};

export const markAsRead = async (conversationId: string): Promise<void> => {
    await api.put(`/conversations/${conversationId}/read-receipt`);
};

export const getUnreadCount = async (): Promise<number> => {
    const conversations = await getConversations();
    return conversations.reduce((acc, conversation) => acc + conversation.unreadCount, 0);
};

export const isUserOnline = async (userId: string): Promise<boolean> => {
    const response = await api.get<boolean>(`/users/${userId}/presence`);
    return response.data;
};

export const getOnlineUsers = async (userIds: string[]): Promise<Record<string, boolean>> => {
    const response = await api.post<Record<string, boolean>>('/presence-queries', userIds);
    return response.data;
};

export const startConversation = async (otherUserId: string): Promise<string> => {
    const response = await api.post<{ conversationId: string }>('/conversations', null, {
        params: { otherUserId }
    });
    return response.data.conversationId;
};
