package com.kvanzi.chatapp.ws.interceptor;

import com.kvanzi.chatapp.auth.component.JwtAuthenticationToken;
import com.kvanzi.chatapp.auth.service.JwtService;
import com.kvanzi.chatapp.user.component.IdentifiableUserDetails;
import com.kvanzi.chatapp.ws.service.WebSocketSessionManager;
import com.kvanzi.chatapp.ws.utils.WebSocketAuthUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandlingException;
import org.springframework.messaging.simp.SimpMessageType;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;

import static org.springframework.messaging.simp.SimpMessageType.HEARTBEAT;
import static org.springframework.messaging.simp.stomp.StompCommand.CONNECT;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuthenticationChannelInterceptor implements ChannelInterceptor {

    private final AuthenticationManager authManager;
    private final JwtService jwtService;
    private final WebSocketSessionManager wsSessionManager;

    private final static String UNAUTHORIZED_MESSAGE = HttpStatus.UNAUTHORIZED.getReasonPhrase();

    @Override
    public Message<?> preSend(@NonNull Message<?> message, @NonNull MessageChannel channel) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);
        StompCommand stompCommand = accessor.getCommand();

        if (stompCommand != null && stompCommand.equals(CONNECT)) {
            return handleConnect(message, accessor);
        }

        validateAuthentication(message, accessor);

        if (stompCommand == null) {
            return handleNullStompCommand(message, accessor);
        }

        return message;
    }

    private Message<?> handleNullStompCommand(Message<?> message, StompHeaderAccessor accessor) {
        SimpMessageType messageType = accessor.getMessageType();

        if (messageType == null) {
            return message;
        }

        if (messageType.equals(HEARTBEAT)) {
            handleHeartbeat(accessor);
            return message;
        }

        return message;
    }

    private void handleHeartbeat(StompHeaderAccessor accessor) {
        String userId = WebSocketAuthUtils.extractSimpUserId(accessor);
        wsSessionManager.extendSession(accessor.getSessionId(), userId);
    }

    private void validateAuthentication(Message<?> message, StompHeaderAccessor accessor) throws MessageHandlingException {
        if (WebSocketAuthUtils.isAuthExpired(accessor)) {
            throw new MessageHandlingException(message, UNAUTHORIZED_MESSAGE);
        }
    }

    private Message<?> handleConnect(Message<?> message, StompHeaderAccessor accessor) {
        Map<String, Object> sessionAttributes = accessor.getSessionAttributes();

        if (sessionAttributes == null) {
            throw new MessageHandlingException(message, "Internal server error. Please contact support");
        }

        IdentifiableUserDetails simpUserDetails = WebSocketAuthUtils.extractSimpUserDetails(accessor);
        String simpUserId = simpUserDetails.getId();

        Optional<String> bearerTokenOpt = WebSocketAuthUtils.extractBearerTokenFromStompHeader(accessor);
        if (bearerTokenOpt.isEmpty()) {
            throw new MessageHandlingException(message, UNAUTHORIZED_MESSAGE);
        }

        String bearerToken = bearerTokenOpt.get();
        Authentication authentication;

        try {
            authentication = authManager.authenticate(new JwtAuthenticationToken(bearerToken));
        } catch (BadCredentialsException e) {
            throw new MessageHandlingException(message, e.getMessage());
        }

        IdentifiableUserDetails userDetails = WebSocketAuthUtils.extractUserDetailsFromAuthentication(authentication);
        String userId = userDetails.getId();

        if (!userId.equals(simpUserId)) {
            throw new MessageHandlingException(message, UNAUTHORIZED_MESSAGE);
        }

        Instant tokenExpiresAt = jwtService.extractExpiredAt(bearerToken);
        WebSocketAuthUtils.setAuthExpiresAt(sessionAttributes, tokenExpiresAt);

        return message;
    }
}
