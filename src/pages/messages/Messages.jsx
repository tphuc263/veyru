import { useState, useEffect, useRef, useCallback } from 'react';
import { useAuthContext } from '../../context/AuthContext';
import { useSocketContext } from '../../context/SocketContext';
import { useSearchParams } from 'react-router-dom';
import {
    getConversations,
    getMessages,
    startConversation,
} from '../../services/messageService';
import {
    getSocket,
    sendSocketMessage,
    markMessagesRead,
    sendTyping,
    sendStopTyping,
    subscribeToSocketEvent,
    unsubscribeFromSocketEvent,
} from '../../services/socketService';
import { Search, Send, ArrowLeft, MoreHorizontal, Smile, Trash2, BellOff, User } from 'lucide-react';
import EmojiPicker from 'emoji-picker-react';
import '../../assets/styles/pages/messagesPage.css';

const Messages = () => {
    const { user } = useAuthContext();
    const { isConnected, onlineUsers, subscribeToMessages } = useSocketContext();
    const [searchParams, setSearchParams] = useSearchParams();

    // State
    const [conversations, setConversations] = useState([]);
    const [activeConversation, setActiveConversation] = useState(null);
    const [messages, setMessages] = useState([]);
    const [newMessage, setNewMessage] = useState('');
    const [searchQuery, setSearchQuery] = useState('');
    const [loading, setLoading] = useState(true);
    const [messagesLoading, setMessagesLoading] = useState(false);
    const [typingUsers, setTypingUsers] = useState(new Set());
    const [showMobileChat, setShowMobileChat] = useState(false);
    const [conversationMenu, setConversationMenu] = useState(null); // convId đang mở menu
    const [mutedConversations, setMutedConversations] = useState(new Set());
    const [showEmojiPicker, setShowEmojiPicker] = useState(false);

    const messagesEndRef = useRef(null);
    const messageInputRef = useRef(null);
    const typingTimeoutRef = useRef(null);
    const convMenuRef = useRef(null);
    const emojiPickerRef = useRef(null);
    // Refs để tránh stale closure trong socket listener
    const activeConversationRef = useRef(null);
    // Guard để tránh gọi handleStartConversation nhiều lần (StrictMode, fast re-render)
    const startingConversationRef = useRef(false);

    // Luôn giữ ref đồng bộ với state/callback mới nhất
    activeConversationRef.current = activeConversation;

    // Handle new message callback - will be used by SocketContext
    const handleNewMessage = useCallback(
        (message) => {
            const currentConversation = activeConversationRef.current;
            console.log('[handleNewMessage] Called with:', message);
            console.log('[handleNewMessage] Current conversation:', currentConversation?.id);

            // Chỉ update messages khi tin nhắn thuộc đúng conversation đang mở
            if (currentConversation?.id === message.conversationId) {
                console.log('[handleNewMessage] Updating messages for current conversation');
                setMessages((prev) => {
                    // Nếu đã có real message này rồi thì bỏ qua
                    if (prev.some((m) => m.id === message.id)) return prev;

                    // Thay thế optimistic message (tin nhắn tạm) bằng real message từ server
                    const optimisticIndex = prev.findIndex(
                        (m) =>
                            m.isOptimistic &&
                            m.senderId === message.senderId &&
                            m.text === message.text
                    );
                    if (optimisticIndex !== -1) {
                        const next = [...prev];
                        next[optimisticIndex] = message;
                        return next;
                    }

                    return [...prev, message];
                });
            }

            // Update danh sách conversations (sidebar)
            setConversations((prev) => {
                const updated = prev.map((conv) => {
                    if (conv.id === message.conversationId) {
                        return {
                            ...conv,
                            lastMessageText: message.text,
                            lastMessageSenderId: message.senderId,
                            lastMessageAt: message.createdAt,
                            unreadCount:
                                currentConversation?.id === message.conversationId
                                    ? 0
                                    : conv.unreadCount + 1,
                        };
                    }
                    return conv;
                });
                return updated.sort(
                    (a, b) => new Date(b.lastMessageAt) - new Date(a.lastMessageAt)
                );
            });

            // Auto mark as read nếu đang xem đúng conversation này
            if (currentConversation?.id === message.conversationId && message.senderId !== user?.id) {
                markMessagesRead(message.conversationId, message.senderId);
            }
        },
        [user?.id]
    );

    // Subscribe to messages from SocketContext
    useEffect(() => {
        if (!subscribeToMessages) return;
        
        console.log('[Messages] Subscribing to socket messages');
        const unsubscribe = subscribeToMessages(handleNewMessage);
        
        return () => {
            console.log('[Messages] Unsubscribing from socket messages');
            unsubscribe();
        };
    }, [subscribeToMessages, handleNewMessage]);

    // Set up typing listeners (these are page-specific, not global)
    useEffect(() => {
        const socket = getSocket();
        if (!socket) return;

        const onUserTyping = ({ userId }) => {
            setTypingUsers((prev) => new Set([...prev, userId]));
        };
        
        const onUserStopTyping = ({ userId }) => {
            setTypingUsers((prev) => {
                const next = new Set(prev);
                next.delete(userId);
                return next;
            });
        };
        
        const onMessagesRead = ({ conversationId }) => {
            setMessages((prev) =>
                prev.map((msg) =>
                    msg.conversationId === conversationId ? { ...msg, read: true } : msg
                )
            );
        };

        subscribeToSocketEvent('user_typing', onUserTyping);
        subscribeToSocketEvent('user_stop_typing', onUserStopTyping);
        subscribeToSocketEvent('messages_read', onMessagesRead);

        return () => {
            unsubscribeFromSocketEvent('user_typing', onUserTyping);
            unsubscribeFromSocketEvent('user_stop_typing', onUserStopTyping);
            unsubscribeFromSocketEvent('messages_read', onMessagesRead);
        };
    }, [isConnected]);

    // Đóng menu/emoji khi click ra ngoài
    useEffect(() => {
        const handleClickOutside = (e) => {
            if (convMenuRef.current && !convMenuRef.current.contains(e.target)) {
                setConversationMenu(null);
            }
            if (emojiPickerRef.current && !emojiPickerRef.current.contains(e.target)) {
                setShowEmojiPicker(false);
            }
        };
        document.addEventListener('mousedown', handleClickOutside);
        return () => document.removeEventListener('mousedown', handleClickOutside);
    }, []);

    // Load conversations
    useEffect(() => {
        loadConversations();
    }, []);

    // Handle URL param for starting a conversation
    useEffect(() => {
        const userId = searchParams.get('userId');
        if (userId && user?.id && !startingConversationRef.current) {
            startingConversationRef.current = true;
            handleStartConversation(userId).finally(() => {
                startingConversationRef.current = false;
            });
            setSearchParams({}, { replace: true });
        }
    }, [searchParams, user?.id]);

    // Handle URL param for restoring active conversation on reload
    useEffect(() => {
        const conversationId = searchParams.get('conversation');
        if (conversationId && conversations.length > 0 && !activeConversation) {
            const conv = conversations.find(c => c.id === parseInt(conversationId, 10) || c.id === conversationId);
            if (conv) {
                handleSelectConversation(conv);
            }
        }
    }, [conversations, searchParams]);

    // Scroll to bottom on new messages
    useEffect(() => {
        scrollToBottom();
    }, [messages]);

    const loadConversations = async () => {
        try {
            setLoading(true);
            const data = await getConversations();
            setConversations(data || []);
        } catch (error) {
            console.error('Failed to load conversations:', error);
        } finally {
            setLoading(false);
        }
    };

    const loadMessages = async (conversationId) => {
        try {
            setMessagesLoading(true);
            const data = await getMessages(conversationId);
            // Messages come in DESC order, reverse for display
            setMessages((data?.content || []).reverse());
        } catch (error) {
            console.error('Failed to load messages:', error);
        } finally {
            setMessagesLoading(false);
        }
    };

    const handleSelectConversation = async (conversation) => {
        setActiveConversation(conversation);
        setShowMobileChat(true);
        
        // Update URL to track the active conversation
        setSearchParams({ conversation: conversation.id }, { replace: true });
        
        await loadMessages(conversation.id);

        // Mark messages as read
        if (conversation.unreadCount > 0) {
            markMessagesRead(conversation.id, conversation.participantId);
            setConversations((prev) =>
                prev.map((conv) =>
                    conv.id === conversation.id ? { ...conv, unreadCount: 0 } : conv
                )
            );
        }

        // Focus input
        setTimeout(() => messageInputRef.current?.focus(), 100);
    };

    const handleStartConversation = async (targetUserId) => {
        try {
            const conversationId = await startConversation(targetUserId);
            // Fetch conversations 1 lần duy nhất, không fetch lần 2
            const data = await getConversations();
            setConversations(data || []);
            const conv = (data || []).find((c) => c.id === conversationId);
            if (conv) {
                handleSelectConversation(conv);
            }
        } catch (error) {
            console.error('Failed to start conversation:', error);
        }
    };

    const handleDeleteConversation = (convId) => {
        setConversations((prev) => prev.filter((c) => c.id !== convId));
        if (activeConversation?.id === convId) {
            setActiveConversation(null);
            setMessages([]);
            setShowMobileChat(false);
            setSearchParams({}, { replace: true });
        }
        setConversationMenu(null);
    };

    const handleMuteConversation = (convId) => {
        setMutedConversations((prev) => {
            const next = new Set(prev);
            if (next.has(convId)) next.delete(convId);
            else next.add(convId);
            return next;
        });
        setConversationMenu(null);
    };

    const onEmojiClick = (emojiData) => {
        setNewMessage((prev) => prev + emojiData.emoji);
        setShowEmojiPicker(false);
        messageInputRef.current?.focus();
    };

    const handleSendMessage = (e) => {
        e.preventDefault();
        if (!newMessage.trim() || !activeConversation) return;

        const text = newMessage.trim();
        console.log('[handleSendMessage] Sending message:', { text, receiverId: activeConversation.participantId });

        // Optimistic update: hiển thị tin nhắn ngay lập tức trước khi server xác nhận
        const optimisticMessage = {
            id: `temp-${Date.now()}`,
            conversationId: activeConversation.id,
            senderId: user?.id,
            receiverId: activeConversation.participantId,
            text,
            read: false,
            createdAt: new Date().toISOString(),
            isOptimistic: true,
        };
        setMessages((prev) => [...prev, optimisticMessage]);

        sendSocketMessage(activeConversation.participantId, text);
        setNewMessage('');

        // Stop typing indicator
        if (typingTimeoutRef.current) {
            clearTimeout(typingTimeoutRef.current);
        }
        sendStopTyping(activeConversation.participantId);
    };

    const handleTyping = (e) => {
        setNewMessage(e.target.value);

        if (!activeConversation) return;

        // Send typing indicator
        sendTyping(activeConversation.participantId);

        // Clear previous timeout
        if (typingTimeoutRef.current) {
            clearTimeout(typingTimeoutRef.current);
        }

        // Set timeout to stop typing
        typingTimeoutRef.current = setTimeout(() => {
            sendStopTyping(activeConversation.participantId);
        }, 2000);
    };

    const scrollToBottom = () => {
        messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
    };

    const formatTime = (dateStr) => {
        if (!dateStr) return '';
        const date = new Date(dateStr);
        const now = new Date();
        const diff = now - date;

        if (diff < 60000) return 'Just now';
        if (diff < 3600000) return `${Math.floor(diff / 60000)}m`;
        if (diff < 86400000) return `${Math.floor(diff / 3600000)}h`;
        if (diff < 604800000) return `${Math.floor(diff / 86400000)}d`;
        return date.toLocaleDateString();
    };

    const formatMessageTime = (dateStr) => {
        if (!dateStr) return '';
        const date = new Date(dateStr);
        return date.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
    };

    const filteredConversations = conversations.filter((conv) =>
        conv.participantUsername?.toLowerCase().includes(searchQuery.toLowerCase())
    );

    const isOnline = (userId) => onlineUsers.has(userId);
    const isTyping = (userId) => typingUsers.has(userId);

    return (
        <div className="messages-container">
            {/* Conversations List */}
            <div className={`conversations-panel ${showMobileChat ? 'mobile-hidden' : ''}`}>
                <div className="conversations-header">
                    <h2>{user?.username}</h2>
                </div>

                <div className="conversations-search">
                    <div className="search-input-wrapper">
                        <Search size={16} />
                        <input
                            type="text"
                            placeholder="Search messages..."
                            value={searchQuery}
                            onChange={(e) => setSearchQuery(e.target.value)}
                        />
                    </div>
                </div>

                <div className="conversations-list">
                    {loading ? (
                        <div className="conversations-loading">
                            <div className="loading-spinner" />
                        </div>
                    ) : filteredConversations.length === 0 ? (
                        <div className="no-conversations">
                            <p>No messages yet</p>
                            <span>Start a conversation from someone's profile</span>
                        </div>
                    ) : (
                        filteredConversations.map((conv) => (
                            <div
                                key={conv.id}
                                className={`conversation-item ${
                                    activeConversation?.id === conv.id ? 'active' : ''
                                } ${conv.unreadCount > 0 ? 'unread' : ''} ${mutedConversations.has(conv.id) ? 'muted' : ''}`}
                                onClick={() => handleSelectConversation(conv)}
                            >
                                <div className="conversation-avatar">
                                    {conv.participantImageUrl ? (
                                        <img
                                            src={conv.participantImageUrl}
                                            alt={conv.participantUsername}
                                        />
                                    ) : (
                                        <div className="avatar-placeholder">
                                            {conv.participantUsername?.[0]?.toUpperCase()}
                                        </div>
                                    )}
                                    {isOnline(conv.participantId) && (
                                        <span className="online-indicator" />
                                    )}
                                </div>
                                <div className="conversation-info">
                                    <div className="conversation-top">
                                        <span className="conversation-name">
                                            {conv.participantUsername}
                                            {mutedConversations.has(conv.id) && (
                                                <span className="muted-icon" title="Muted">🔇</span>
                                            )}
                                        </span>
                                        <span className="conversation-time">
                                            {formatTime(conv.lastMessageAt)}
                                        </span>
                                    </div>
                                    <div className="conversation-preview">
                                        {isTyping(conv.participantId) ? (
                                            <span className="typing-indicator-text">typing...</span>
                                        ) : (
                                            <span className={conv.unreadCount > 0 ? 'bold' : ''}>
                                                {conv.lastMessageSenderId === user?.id ? 'You: ' : ''}
                                                {conv.lastMessageText || 'No messages yet'}
                                            </span>
                                        )}
                                        {conv.unreadCount > 0 && (
                                            <span className="unread-badge">{conv.unreadCount}</span>
                                        )}
                                    </div>
                                </div>

                                {/* Options button */}
                                <div className="conversation-menu-wrapper" ref={conversationMenu === conv.id ? convMenuRef : null}>
                                    <button
                                        className="conversation-options-btn"
                                        onClick={(e) => {
                                            e.stopPropagation();
                                            setConversationMenu(conversationMenu === conv.id ? null : conv.id);
                                        }}
                                    >
                                        <MoreHorizontal size={18} />
                                    </button>
                                    {conversationMenu === conv.id && (
                                        <div className="conversation-dropdown">
                                            <button
                                                className="dropdown-item"
                                                onClick={(e) => {
                                                    e.stopPropagation();
                                                    setConversationMenu(null);
                                                }}
                                            >
                                                <User size={15} />
                                                View Profile
                                            </button>
                                            <button
                                                className="dropdown-item"
                                                onClick={(e) => {
                                                    e.stopPropagation();
                                                    handleMuteConversation(conv.id);
                                                }}
                                            >
                                                <BellOff size={15} />
                                                {mutedConversations.has(conv.id) ? 'Unmute' : 'Mute notifications'}
                                            </button>
                                            <button
                                                className="dropdown-item danger"
                                                onClick={(e) => {
                                                    e.stopPropagation();
                                                    handleDeleteConversation(conv.id);
                                                }}
                                            >
                                                <Trash2 size={15} />
                                                Delete conversation
                                            </button>
                                        </div>
                                    )}
                                </div>
                            </div>
                        ))
                    )}
                </div>
            </div>

            {/* Chat Panel */}
            <div className={`chat-panel ${showMobileChat ? 'mobile-visible' : ''}`}>
                {activeConversation ? (
                    <>
                        <div className="chat-header">
                            <button
                                className="back-button"
                                onClick={() => {
                                    setShowMobileChat(false);
                                    setActiveConversation(null);
                                    setSearchParams({}, { replace: true });
                                }}
                            >
                                <ArrowLeft size={24} />
                            </button>
                            <div className="chat-header-user">
                                <div className="chat-header-avatar">
                                    {activeConversation.participantImageUrl ? (
                                        <img
                                            src={activeConversation.participantImageUrl}
                                            alt={activeConversation.participantUsername}
                                        />
                                    ) : (
                                        <div className="avatar-placeholder small">
                                            {activeConversation.participantUsername?.[0]?.toUpperCase()}
                                        </div>
                                    )}
                                    {isOnline(activeConversation.participantId) && (
                                        <span className="online-indicator small" />
                                    )}
                                </div>
                                <div className="chat-header-info">
                                    <span className="chat-header-name">
                                        {activeConversation.participantUsername}
                                    </span>
                                    <span className="chat-header-status">
                                        {isTyping(activeConversation.participantId)
                                            ? 'typing...'
                                            : isOnline(activeConversation.participantId)
                                            ? 'Active now'
                                            : 'Offline'}
                                    </span>
                                </div>
                            </div>
                        </div>

                        <div className="chat-messages">
                            {messagesLoading ? (
                                <div className="messages-loading">
                                    <div className="loading-spinner" />
                                </div>
                            ) : messages.length === 0 ? (
                                <div className="no-messages">
                                    <div className="no-messages-avatar">
                                        {activeConversation.participantImageUrl ? (
                                            <img
                                                src={activeConversation.participantImageUrl}
                                                alt={activeConversation.participantUsername}
                                            />
                                        ) : (
                                            <div className="avatar-placeholder large">
                                                {activeConversation.participantUsername?.[0]?.toUpperCase()}
                                            </div>
                                        )}
                                    </div>
                                    <h3>{activeConversation.participantUsername}</h3>
                                    <p>Send your first message to start the conversation!</p>
                                </div>
                            ) : (
                                <>
                                    {messages.map((msg, index) => {
                                        const isMine = msg.senderId === user?.id;
                                        const showAvatar =
                                            !isMine &&
                                            (index === 0 ||
                                                messages[index - 1]?.senderId !== msg.senderId);

                                        return (
                                            <div
                                                key={msg.id}
                                                className={`message-wrapper ${
                                                    isMine ? 'sent' : 'received'
                                                }`}
                                            >
                                                {!isMine && showAvatar && (
                                                    <div className="message-avatar">
                                                        {activeConversation.participantImageUrl ? (
                                                            <img
                                                                src={
                                                                    activeConversation.participantImageUrl
                                                                }
                                                                alt=""
                                                            />
                                                        ) : (
                                                            <div className="avatar-placeholder tiny">
                                                                {activeConversation.participantUsername?.[0]?.toUpperCase()}
                                                            </div>
                                                        )}
                                                    </div>
                                                )}
                                                <div
                                                    className={`message-bubble ${
                                                        isMine ? 'sent' : 'received'
                                                    }`}
                                                >
                                                    <p>{msg.text}</p>
                                                    <span className="message-time">
                                                        {formatMessageTime(msg.createdAt)}
                                                        {isMine && (
                                                            <span className="read-status">
                                                                {msg.read ? ' ✓✓' : ' ✓'}
                                                            </span>
                                                        )}
                                                    </span>
                                                </div>
                                            </div>
                                        );
                                    })}
                                    <div ref={messagesEndRef} />
                                </>
                            )}
                        </div>

                        <form className="chat-input" onSubmit={handleSendMessage}>
                            {/* Emoji picker */}
                            <div className="emoji-wrapper" ref={emojiPickerRef}>
                                <button
                                    type="button"
                                    className="emoji-toggle-btn"
                                    onClick={() => setShowEmojiPicker((prev) => !prev)}
                                    title="Emoji"
                                >
                                    <Smile size={20} />
                                </button>
                                {showEmojiPicker && (
                                    <div className="emoji-picker-container">
                                        <EmojiPicker
                                            onEmojiClick={onEmojiClick}
                                            width={350}
                                            height={400}
                                            searchPlaceHolder="Search emoji..."
                                            previewConfig={{ showPreview: false }}
                                            skinTonesDisabled
                                            lazyLoadEmojis
                                        />
                                    </div>
                                )}
                            </div>
                            <input
                                ref={messageInputRef}
                                type="text"
                                placeholder="Message..."
                                value={newMessage}
                                onChange={handleTyping}
                                autoFocus
                            />
                            <button
                                type="submit"
                                className={`send-button ${newMessage.trim() ? 'active' : ''}`}
                                disabled={!newMessage.trim()}
                            >
                                <Send size={20} />
                            </button>
                        </form>
                    </>
                ) : (
                    <div className="no-chat-selected">
                        <div className="no-chat-icon">
                            <Send size={48} />
                        </div>
                        <h2>Your Messages</h2>
                        <p>Send private messages to a friend</p>
                    </div>
                )}
            </div>
        </div>
    );
};

export default Messages;
