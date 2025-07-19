package com.kvanzi.chatapp.ws.interceptor;

import com.kvanzi.chatapp.auth.component.JwtAuthenticationToken;
import com.kvanzi.chatapp.user.component.IdentifiableUserDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandlingException;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;

import java.util.Map;

import static com.kvanzi.chatapp.auth.service.JwtService.BEARER_PREFIX;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;

@Slf4j
@RequiredArgsConstructor
public class AuthChannelInterceptor implements ChannelInterceptor {

    private final AuthenticationManager authManager;
    private final static String USER_HEADER = "simpUser";

    @Override
    public Message<?> preSend(@NonNull Message<?> message, @NonNull MessageChannel channel) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);

        if (accessor.getCommand() == null) {
            return message;
        }

        authenticateUserIfAvailable(message, accessor);
        return message;
    }

    private void authenticateUserIfAvailable(Message<?> message, StompHeaderAccessor accessor) {
        Map<String, Object> sessionAttributes = accessor.getSessionAttributes();
        if (sessionAttributes == null) {
            return;
        }

        String authorizationHeader = accessor.getFirstNativeHeader(AUTHORIZATION);
        if (authorizationHeader == null || !authorizationHeader.startsWith(BEARER_PREFIX)) {
            setUnauthenticated(sessionAttributes);
            return;
        }

        String token = authorizationHeader.substring(BEARER_PREFIX.length()).trim();

        try {
            Authentication auth = authManager.authenticate(new JwtAuthenticationToken(token));
            IdentifiableUserDetails userDetails = (IdentifiableUserDetails) auth.getPrincipal();
            String userId = userDetails.getId();

            JwtAuthenticationToken simpUserAuthToken = (JwtAuthenticationToken) accessor.getHeader(USER_HEADER);
            if (simpUserAuthToken == null) {
                setUnauthenticated(sessionAttributes);
                return;
            }

            IdentifiableUserDetails simpUserDetails = (IdentifiableUserDetails) simpUserAuthToken.getPrincipal();
            String simpUserId = simpUserDetails.getId();

            if (!userId.equals(simpUserId)) { // If user tries to access resource from other account
                setUnauthenticated(sessionAttributes);
                return;
            }

            setAuthenticated(sessionAttributes, userId);
        } catch (BadCredentialsException e) {
            setUnauthenticated(sessionAttributes);
        } catch (RuntimeException e) {
            setUnauthenticated(sessionAttributes);
            throw new MessageHandlingException(message, "Internal server error");
        }
    }

    private void setUnauthenticated(Map<String, Object> sessionAttributes) {
        sessionAttributes.put("authenticated", false);
        sessionAttributes.remove("userId");
    }

    private void setAuthenticated(Map<String, Object> sessionAttributes, String userId) {
        sessionAttributes.put("authenticated", true);
        sessionAttributes.put("userId", userId);
    }
}
