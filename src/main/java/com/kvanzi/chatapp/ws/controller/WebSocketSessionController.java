package com.kvanzi.chatapp.ws.controller;

import com.kvanzi.chatapp.auth.component.JwtAuthenticationToken;
import com.kvanzi.chatapp.auth.exception.CustomAuthenticationException;
import com.kvanzi.chatapp.auth.service.JwtService;
import com.kvanzi.chatapp.user.component.IdentifiableUserDetails;
import com.kvanzi.chatapp.ws.utils.WebSocketAuthUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;

import java.time.Instant;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class WebSocketSessionController {

    private final AuthenticationManager authManager;
    private final JwtService jwtService;

    @MessageMapping("/session.extendAuthentication")
    public void extendUserAuthentication(StompHeaderAccessor accessor) {
        Map<String, Object> sessionAttributes = accessor.getSessionAttributes();
        if (sessionAttributes == null) {
            throw new IllegalStateException("Internal server error. Please contact support");
        }

        String authToken = extractAuthToken(accessor);
        IdentifiableUserDetails userDetails = authenticate(authToken);

        String userId = userDetails.getId();
        String simpUserId = WebSocketAuthUtils.extractSimpUserId(accessor);

        if (!userId.equals(simpUserId)) {
            throw new CustomAuthenticationException("Invalid auth token");
        }

        Instant expiresAt = jwtService.extractExpiredAt(authToken);
        WebSocketAuthUtils.setAuthExpiresAt(sessionAttributes, expiresAt);
    }

    private String extractAuthToken(StompHeaderAccessor accessor) {
        return WebSocketAuthUtils.extractBearerTokenFromStompHeader(accessor)
                .orElseThrow(() -> new CustomAuthenticationException("No auth token provided"));
    }

    private IdentifiableUserDetails authenticate(String token) {
        Authentication authentication = authManager.authenticate(new JwtAuthenticationToken(token));
        return (IdentifiableUserDetails) authentication.getPrincipal();
    }
}
