package com.kvanzi.chatapp.ws.listener;

import com.kvanzi.chatapp.ws.service.UserStatusService;
import com.kvanzi.chatapp.ws.service.WebSocketSessionManager;
import com.kvanzi.chatapp.ws.utils.WebSocketUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import static com.kvanzi.chatapp.user.enumeration.UserStatus.OFFLINE;

@Component
@RequiredArgsConstructor
public class WebSocketEventListener {

    private final WebSocketSessionManager sessionManager;
    private final UserStatusService userStatusService;

    @EventListener
    public void onSessionDisconnect(SessionDisconnectEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        String userId = WebSocketUtils.extractUserIdFromAccessor(accessor)
                .orElseThrow(() -> new IllegalStateException("Unauthorized"));

        sessionManager.closeSession(accessor.getSessionId(), userId);

        if (sessionManager.getUserSessionsCount(userId) == 0) {
            userStatusService.setUserStatus(userId, OFFLINE);
        }
    }
}
