package com.kvanzi.chatapp.ws.component;

import com.kvanzi.chatapp.user.service.UserStatusService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketEventListener {

    private final UserStatusService userStatusService;

    @EventListener
    public void sessionConnectEvent(SessionConnectEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = accessor.getSessionId();
        Optional<String> userIdOpt = extractUserIdFromAccessor(accessor);

        if (sessionId != null && userIdOpt.isPresent()) {
            userStatusService.userConnected(userIdOpt.get(), sessionId);
        }
    }

    @EventListener
    public void sessionDisconnectEvent(SessionDisconnectEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = accessor.getSessionId();
        Optional<String> userIdOpt = extractUserIdFromAccessor(accessor);

        if (sessionId != null && userIdOpt.isPresent()) {
            userStatusService.userDisconnected(userIdOpt.get(), sessionId);
        }
    }

    @EventListener
    public void sessionConnectedEvent(SessionConnectedEvent event) {

    }

    private Optional<String> extractUserIdFromAccessor(StompHeaderAccessor accessor) {
        var sessionAttributes = accessor.getSessionAttributes();
        if (sessionAttributes == null) {
            return Optional.empty();
        }

        Object userIdObj = sessionAttributes.get("userId");
        if (!(userIdObj instanceof String)) {
            return Optional.empty();
        }

        return Optional.of((String) userIdObj);
    }
}
