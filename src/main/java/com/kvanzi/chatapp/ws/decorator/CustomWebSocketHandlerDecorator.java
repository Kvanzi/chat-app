package com.kvanzi.chatapp.ws.decorator;

import com.kvanzi.chatapp.ws.service.WebSocketSessionManager;
import com.kvanzi.chatapp.ws.utils.WebSocketAuthUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.WebSocketHandlerDecorator;

@Slf4j
public class CustomWebSocketHandlerDecorator extends WebSocketHandlerDecorator {

    private final WebSocketSessionManager sessionManager;

    public CustomWebSocketHandlerDecorator(WebSocketHandler delegate, WebSocketSessionManager sessionManager) {
        super(delegate);
        this.sessionManager = sessionManager;
    }

    @Override
    public void afterConnectionEstablished(@NonNull WebSocketSession session) throws Exception {
        sessionManager.addSession(session, WebSocketAuthUtils.extractSimpUserId(session));
        super.afterConnectionEstablished(session);
    }

    @Override
    public void afterConnectionClosed(@NonNull WebSocketSession session, @NonNull CloseStatus closeStatus) throws Exception {
        sessionManager.closeSession(session.getId(), WebSocketAuthUtils.extractSimpUserId(session));
        super.afterConnectionClosed(session, closeStatus);
    }
}