package com.kvanzi.chatapp.ws.component;

import com.kvanzi.chatapp.auth.component.JwtAuthenticationToken;
import com.kvanzi.chatapp.auth.service.JwtService;
import com.kvanzi.chatapp.user.component.IdentifiableUserDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.lang.NonNull;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandlingException;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketChannelInterceptor implements ChannelInterceptor {

    private final AuthenticationManager authManager;

    @Override
    public Message<?> preSend(@NonNull Message<?> message, @NonNull MessageChannel channel) {
        var accessor = StompHeaderAccessor.wrap(message);
        StompCommand command = accessor.getCommand();
        if (command == null) {
            return null;
        }

        return switch (command) {
            case CONNECT -> handleConnect(message, accessor);
            case SEND -> handleSend(message, accessor);
            default -> message;
        };
    }

    private Message<?> handleConnect(Message<?> message, StompHeaderAccessor accessor) {
        String authorizationHeader = accessor.getFirstNativeHeader(HttpHeaders.AUTHORIZATION);
        if (authorizationHeader == null || !authorizationHeader.startsWith(JwtService.BEARER_PREFIX)) {
            throw new MessageHandlingException(message, "Missing or invalid authorization header");
        }

        String token = authorizationHeader.substring(JwtService.BEARER_PREFIX.length()).trim();
        try {
            var jwtAuthToken = new JwtAuthenticationToken(token);
            Authentication authentication = authManager.authenticate(jwtAuthToken);

            String userId = ((IdentifiableUserDetails) authentication.getPrincipal()).getId();
            accessor.getSessionAttributes().put("userId", userId); // FIXME: ADD ADDITIONAL CHECK

            return message;
        } catch (BadCredentialsException e) {
            throw new MessageHandlingException(message, e.getMessage());
        } catch (RuntimeException e) {
            throw new MessageHandlingException(message, "Internal server error");
        }
    }

    private Message<?> handleSend(Message<?> message, StompHeaderAccessor accessor) {
        String sessionId = accessor.getSessionId();

        // FIXME: IMPLEMENT auth check

        return message;
    }
}
