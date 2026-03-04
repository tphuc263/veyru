package share_app.tphucshareapp.service.message;

import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.listener.ConnectListener;
import com.corundumstudio.socketio.listener.DataListener;
import com.corundumstudio.socketio.listener.DisconnectListener;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import share_app.tphucshareapp.dto.response.message.MessageResponse;
import share_app.tphucshareapp.security.jwt.JwtUtils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
@Slf4j
public class SocketIOHandler {

    private final SocketIOServer socketIOServer;
    private final MessageService messageService;
    private final JwtUtils jwtUtils;

    // Map userId -> SocketIOClient
    private final Map<String, SocketIOClient> onlineUsers = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        socketIOServer.addConnectListener(onConnect());
        socketIOServer.addDisconnectListener(onDisconnect());
        socketIOServer.addEventListener("send_message", Map.class, onSendMessage());
        socketIOServer.addEventListener("mark_read", Map.class, onMarkRead());
        socketIOServer.addEventListener("typing", Map.class, onTyping());
        socketIOServer.addEventListener("stop_typing", Map.class, onStopTyping());

        socketIOServer.start();
        log.info("Socket.IO server started on port {}", socketIOServer.getConfiguration().getPort());
    }

    private ConnectListener onConnect() {
        return client -> {
            String token = extractToken(client);
            if (token == null || !jwtUtils.validateToken(token)) {
                log.warn("Socket.IO: invalid token, disconnecting client");
                client.disconnect();
                return;
            }

            String email = jwtUtils.getEmailFromToken(token);
            // Store userId from handshake query
            String userId = client.getHandshakeData().getSingleUrlParam("userId");
            if (userId == null || userId.isEmpty()) {
                log.warn("Socket.IO: no userId provided, disconnecting client");
                client.disconnect();
                return;
            }

            client.set("userId", userId);
            onlineUsers.put(userId, client);
            log.info("Socket.IO: User {} connected (session: {})", userId, client.getSessionId());

            // Notify others that this user is online
            socketIOServer.getBroadcastOperations().sendEvent("user_online", userId);
        };
    }

    private DisconnectListener onDisconnect() {
        return client -> {
            String userId = client.get("userId");
            if (userId != null) {
                onlineUsers.remove(userId);
                log.info("Socket.IO: User {} disconnected", userId);

                // Notify others that this user is offline
                socketIOServer.getBroadcastOperations().sendEvent("user_offline", userId);
            }
        };
    }

    @SuppressWarnings("unchecked")
    private DataListener<Map> onSendMessage() {
        return (client, data, ackSender) -> {
            try {
                String senderId = client.get("userId");
                String receiverId = (String) data.get("receiverId");
                String text = (String) data.get("text");

                if (senderId == null || receiverId == null || text == null || text.trim().isEmpty()) {
                    log.warn("Socket.IO: invalid message data");
                    return;
                }

                // Save message via service
                MessageResponse messageResponse = messageService.sendMessage(senderId, receiverId, text.trim());

                // Send to sender (confirmation)
                client.sendEvent("new_message", messageResponse);

                // Send to receiver if online
                SocketIOClient receiverClient = onlineUsers.get(receiverId);
                if (receiverClient != null && receiverClient.isChannelOpen()) {
                    receiverClient.sendEvent("new_message", messageResponse);
                }

                log.debug("Socket.IO: Message sent from {} to {}", senderId, receiverId);
            } catch (Exception e) {
                log.error("Socket.IO: Error sending message", e);
                client.sendEvent("error", Map.of("message", "Failed to send message"));
            }
        };
    }

    @SuppressWarnings("unchecked")
    private DataListener<Map> onMarkRead() {
        return (client, data, ackSender) -> {
            try {
                String userId = client.get("userId");
                String conversationId = (String) data.get("conversationId");
                
                if (userId == null || conversationId == null) return;

                messageService.markMessagesAsRead(conversationId, userId);

                // Notify the other user that messages were read
                String otherUserId = (String) data.get("otherUserId");
                if (otherUserId != null) {
                    SocketIOClient otherClient = onlineUsers.get(otherUserId);
                    if (otherClient != null && otherClient.isChannelOpen()) {
                        otherClient.sendEvent("messages_read", Map.of(
                                "conversationId", conversationId,
                                "readByUserId", userId
                        ));
                    }
                }
            } catch (Exception e) {
                log.error("Socket.IO: Error marking messages as read", e);
            }
        };
    }

    @SuppressWarnings("unchecked")
    private DataListener<Map> onTyping() {
        return (client, data, ackSender) -> {
            String userId = client.get("userId");
            String receiverId = (String) data.get("receiverId");
            if (receiverId == null) return;

            SocketIOClient receiverClient = onlineUsers.get(receiverId);
            if (receiverClient != null && receiverClient.isChannelOpen()) {
                receiverClient.sendEvent("user_typing", Map.of("userId", userId));
            }
        };
    }

    @SuppressWarnings("unchecked")
    private DataListener<Map> onStopTyping() {
        return (client, data, ackSender) -> {
            String userId = client.get("userId");
            String receiverId = (String) data.get("receiverId");
            if (receiverId == null) return;

            SocketIOClient receiverClient = onlineUsers.get(receiverId);
            if (receiverClient != null && receiverClient.isChannelOpen()) {
                receiverClient.sendEvent("user_stop_typing", Map.of("userId", userId));
            }
        };
    }

    public boolean isUserOnline(String userId) {
        SocketIOClient client = onlineUsers.get(userId);
        return client != null && client.isChannelOpen();
    }

    private String extractToken(SocketIOClient client) {
        // Try from query parameter
        String token = client.getHandshakeData().getSingleUrlParam("token");
        if (token != null && !token.isEmpty()) {
            return token;
        }

        // Try from Authorization header
        String authHeader = client.getHandshakeData().getHttpHeaders().get("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }

        return null;
    }
}
