package com.kvanzi.chatapp.ws.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class WebSocketSessionService {

    private final static Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();

    public static void addSession(WebSocketSession session) {
        if (session != null && session.isOpen()) {
            String sessionId = session.getId();
            sessions.putIfAbsent(sessionId, session);
        }
    }

    public static void closeSession(WebSocketSession session) {
        if (session != null) {
            closeSession(session.getId());
        }
    }

    public static void closeSession(String sessionId) {
        if (sessionId == null) {
            return;
        }

        WebSocketSession session = sessions.get(sessionId);
        if (session != null && session.isOpen()) {
            try {
                session.close(CloseStatus.NORMAL);
            } catch (IOException e) {
                log.error("Error occurred while closing web socket session. Error: ", e);
            }
        }
        sessions.remove(sessionId);
    }
}
