package com.kvanzi.chatapp.ws.decorator;

import com.kvanzi.chatapp.auth.component.JwtAuthenticationToken;
import com.kvanzi.chatapp.user.component.IdentifiableUserDetails;
import com.kvanzi.chatapp.ws.service.WebSocketSessionManager;
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
        sessionManager.addSession(session, extractUserId(session));

        super.afterConnectionEstablished(session);
    }

    @Override
    public void handleTransportError(@NonNull WebSocketSession session, @NonNull Throwable exception) throws Exception {
        log.debug("[TRANSPORT ERROR]::", exception);

        sessionManager.closeSession(session.getId(), extractUserId(session));
        super.handleTransportError(session, exception);
    }

    @Override
    public void afterConnectionClosed(@NonNull WebSocketSession session, @NonNull CloseStatus closeStatus) throws Exception {
        sessionManager.closeSession(session.getId(), extractUserId(session));

        super.afterConnectionClosed(session, closeStatus);
    }

    private String extractUserId(WebSocketSession session) {
        return extractUserDetails(session).getId();
    }

    private IdentifiableUserDetails extractUserDetails(WebSocketSession session) {
        JwtAuthenticationToken authenticationToken = (JwtAuthenticationToken) session.getPrincipal();

        if (authenticationToken == null) {
            throw new IllegalStateException("WebSocket session is not authenticated");
        }

        return (IdentifiableUserDetails) authenticationToken.getPrincipal();
    }
}
