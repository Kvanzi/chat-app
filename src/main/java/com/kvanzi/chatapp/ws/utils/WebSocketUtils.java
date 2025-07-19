package com.kvanzi.chatapp.ws.utils;

import org.springframework.messaging.simp.stomp.StompHeaderAccessor;

import java.util.Optional;

public class WebSocketUtils {

    public static Optional<String> extractUserIdFromAccessor(StompHeaderAccessor accessor) {
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
