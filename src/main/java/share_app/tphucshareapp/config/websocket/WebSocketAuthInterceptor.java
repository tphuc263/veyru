package share_app.tphucshareapp.config.websocket;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;
import share_app.tphucshareapp.security.jwt.JwtUtils;
import share_app.tphucshareapp.security.userdetails.StompPrincipal;

import java.util.List;

@Component
@RequiredArgsConstructor
public class WebSocketAuthInterceptor implements ChannelInterceptor {

    private final JwtUtils jwtUtils;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
            List<String> authorization = accessor.getNativeHeader("Authorization");
            
            if (authorization != null && !authorization.isEmpty()) {
                String bearerToken = authorization.get(0);
                if (bearerToken.startsWith("Bearer ")) {
                    String token = bearerToken.substring(7);
                    try {
                        if (jwtUtils.validateToken(token)) {
                            String userId = jwtUtils.getUserIdFromToken(token);
                            accessor.setUser(new StompPrincipal(userId));
                            return message;
                        }
                    } catch (Exception e) {
                        throw new IllegalArgumentException("Invalid WebSocket authentication token", e);
                    }
                }
            }
            throw new IllegalArgumentException("Missing or invalid Authorization header");
        }
        
        return message;
    }
}
