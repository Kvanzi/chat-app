package com.kvanzi.chatapp.ws.listener;

import com.kvanzi.chatapp.user.enumeration.UserStatus;
import com.kvanzi.chatapp.ws.service.UserStatusService;
import com.kvanzi.chatapp.ws.service.WebSocketSessionManager;
import com.kvanzi.chatapp.ws.utils.WebSocketAuthUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import static com.kvanzi.chatapp.user.enumeration.UserStatus.OFFLINE;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketEventListener {

    private final WebSocketSessionManager sessionManager;
    private final UserStatusService userStatusService;

    @EventListener
    public void onSessionDisconnect(SessionDisconnectEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        String userId = WebSocketAuthUtils.extractSimpUserId(accessor);

        sessionManager.closeSession(accessor.getSessionId(), userId);

        UserStatus currentStatus = userStatusService.getUserStatus(userId);
        if (currentStatus != OFFLINE && sessionManager.getUserSessionsCount(userId) == 0) {
            userStatusService.setUserStatus(userId, OFFLINE);
        }
    }
}
