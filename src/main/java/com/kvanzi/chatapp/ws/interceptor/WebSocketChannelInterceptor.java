package com.kvanzi.chatapp.ws.interceptor;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandlingException;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;

import java.util.Map;

import static org.springframework.messaging.simp.stomp.StompCommand.DISCONNECT;
import static org.springframework.messaging.simp.stomp.StompCommand.UNSUBSCRIBE;

@Slf4j
@RequiredArgsConstructor
public class WebSocketChannelInterceptor implements ChannelInterceptor {

    private final String USER_DESTINATION_PREFIX;

    @Override
    public Message<?> preSend(@NonNull Message<?> message, @NonNull MessageChannel channel) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);
        StompCommand command = accessor.getCommand();

        if (command == null) {
            return message;
        }

        if (requiresAuthentication(command) && !isAuthenticated(accessor)) {
            throw new MessageHandlingException(message, "Unauthorized");
        }

        return switch (command) {
            case SUBSCRIBE -> handleSubscribe(message, accessor);
            default -> message;
        };
    }

    private boolean requiresAuthentication(StompCommand command) {
        return command != DISCONNECT && command != UNSUBSCRIBE;
    }

    private Message<?> handleSubscribe(Message<?> message, StompHeaderAccessor accessor) {
        String destination = accessor.getDestination();
        if (destination == null) {
            return message;
        }

        if (destination.startsWith(USER_DESTINATION_PREFIX)) {
            String userId = getUserId(accessor);
            String userPrefix = USER_DESTINATION_PREFIX + userId;

            if (!destination.startsWith(userPrefix + "/") && !destination.equals(userPrefix)) {
                log.warn("Unauthorized access attempt: user {} tried to access destination {}", userId, destination);
                throw new MessageHandlingException(message, "Access denied to private queue");
            }
        }

        return message;
    }

    private String getUserId(StompHeaderAccessor accessor) {
        Map<String, Object> sessionAttributes = accessor.getSessionAttributes();
        if (sessionAttributes == null) {
            return null;
        }

        return (String) sessionAttributes.get("userId");
    }

    private boolean isAuthenticated(StompHeaderAccessor accessor) {
        Map<String, Object> sessionAttributes = accessor.getSessionAttributes();
        if (sessionAttributes == null) {
            return false;
        }

        Boolean isAuthenticated = (Boolean) sessionAttributes.get("authenticated");
        return isAuthenticated != null ? isAuthenticated : false;
    }
}
