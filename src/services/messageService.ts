import api from '../config/ApiConfig';

export interface ConversationDTO {
    id: string;
    participantId: string;
    participantUsername: string;
    participantImageUrl: string;
    lastMessageText: string;
    lastMessageSenderId: string;
    lastMessageAt: string;
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
    content: T[];
    totalElements: number;
    totalPages: number;
    number: number;
    size: number;
    last: boolean;
}

export const getConversations = async (): Promise<ConversationDTO[]> => {
    const response = await api.get('/messages/conversations');
    return response.data as ConversationDTO[];
};

export const getMessages = async (
    conversationId: string,
    page: number = 0,
    size: number = 50
): Promise<PageResponse<MessageDTO>> => {
    const response = await api.get(`/messages/conversations/${conversationId}`, {
        params: { page, size },
    });
    return response.data as PageResponse<MessageDTO>;
};

export const sendMessage = async (receiverId: string, text: string): Promise<MessageDTO> => {
    const response = await api.post('/messages/send', { receiverId, text });
    return response.data as MessageDTO;
};

export const markAsRead = async (conversationId: string): Promise<void> => {
    await api.post(`/messages/conversations/${conversationId}/read`);
};

export const getUnreadCount = async (): Promise<number> => {
    const response = await api.get('/messages/unread-count');
    return response.data as number;
};

export const isUserOnline = async (userId: string): Promise<boolean> => {
    const response = await api.get(`/messages/online/${userId}`);
    return response.data as boolean;
};

export const getOnlineUsers = async (userIds: string[]): Promise<Record<string, boolean>> => {
    const response = await api.post('/messages/online-users', userIds);
    return response.data as Record<string, boolean>;
};

export const startConversation = async (otherUserId: string): Promise<string> => {
    const response = await api.post(`/messages/conversations/start/${otherUserId}`);
    return (response.data as { conversationId: string }).conversationId;
};
