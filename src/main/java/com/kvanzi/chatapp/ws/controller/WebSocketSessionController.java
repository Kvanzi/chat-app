package com.kvanzi.chatapp.ws.controller;

import com.kvanzi.chatapp.ws.service.WebSocketSessionManager;
import com.kvanzi.chatapp.ws.utils.WebSocketUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class SessionController {

    private final WebSocketSessionManager sessionManager;

    @MessageMapping("/session.extend")
    public void extendUserSession(StompHeaderAccessor accessor) {
        String userId = WebSocketUtils.extractUserIdFromAccessor(accessor)
                .orElseThrow(() -> new IllegalStateException("Unauthenticated"));

        sessionManager.extendSession(accessor.getSessionId(), userId);
    }
}
