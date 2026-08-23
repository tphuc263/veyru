package com.veyru.config.websocket;

import com.veyru.adapter.in.security.userdetails.StompPrincipal;
import com.veyru.adapter.security.JwtUtils;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;

@Component
public class WebSocketAuthInterceptor implements ChannelInterceptor {
  private static final Logger log = LoggerFactory.getLogger(WebSocketAuthInterceptor.class);
  private final JwtUtils jwtUtils;

  @Override
  public Message<?> preSend(Message<?> message, MessageChannel channel) {
    StompHeaderAccessor accessor =
        MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
    if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
      List<String> authorization = accessor.getNativeHeader("Authorization");
      if (authorization != null && !authorization.isEmpty()) {
        String bearerToken = authorization.get(0);
        if (bearerToken.startsWith("Bearer ")) {
          String token = bearerToken.substring(7);

          if (jwtUtils.validateToken(token)) {
            String userId = jwtUtils.getUserIdFromToken(token);
            accessor.setUser(new StompPrincipal(userId));
            return message;
          } else {
            log.warn("Invalid JWT token during WebSocket handshake");
          }

        } else {
          log.warn("Authorization header does not start with Bearer");
        }
      } else {
        log.warn(
            "Missing Authorization header in STOMP CONNECT frame. Headers present: {}",
            accessor.toNativeHeaderMap());
      }
      throw new IllegalArgumentException("Missing or invalid Authorization header");
    }
    return message;
  }

  public WebSocketAuthInterceptor(final JwtUtils jwtUtils) {
    this.jwtUtils = jwtUtils;
  }
}
