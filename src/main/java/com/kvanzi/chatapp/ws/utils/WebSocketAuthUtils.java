package com.kvanzi.chatapp.ws.utils;

import com.kvanzi.chatapp.auth.component.JwtAuthenticationToken;
import com.kvanzi.chatapp.auth.service.JwtService;
import com.kvanzi.chatapp.user.component.IdentifiableUserDetails;
import org.springframework.http.HttpHeaders;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.security.core.Authentication;
import org.springframework.web.socket.WebSocketSession;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;

public class WebSocketAuthUtils {

    private WebSocketAuthUtils() {}

    public static String extractSimpUserId(StompHeaderAccessor accessor) {
        return extractSimpUserDetails(accessor).getId();
    }

    public static String extractSimpUserId(WebSocketSession session) {
        return extractSessionUserDetails(session).getId();
    }

    public static boolean isAuthExpired(StompHeaderAccessor accessor) {
        Map<String, Object> sessionAttributes = accessor.getSessionAttributes();

        if (sessionAttributes == null || !sessionAttributes.containsKey("authExpiresAt")) {
            return false;
        }

        Instant authExpiresAt = (Instant) sessionAttributes.get("authExpiresAt");
        return authExpiresAt.isBefore(Instant.now());
    }

    public static IdentifiableUserDetails extractSimpUserDetails(StompHeaderAccessor accessor) {
        JwtAuthenticationToken authToken = extractSimpUserJwtAuthenticationToken(accessor);
        return (IdentifiableUserDetails) authToken.getPrincipal();
    }

    public static IdentifiableUserDetails extractSessionUserDetails(WebSocketSession session) {
        JwtAuthenticationToken authToken = extractSessionPrincipalJwtAuthenticationToken(session);
        return (IdentifiableUserDetails) authToken.getPrincipal();
    }

    public static JwtAuthenticationToken extractSimpUserJwtAuthenticationToken(StompHeaderAccessor accessor) {
        return (JwtAuthenticationToken) accessor.getUser();
    }

    public static JwtAuthenticationToken extractSessionPrincipalJwtAuthenticationToken(WebSocketSession session) {
        return (JwtAuthenticationToken) session.getPrincipal();
    }

    public static IdentifiableUserDetails extractUserDetailsFromAuthentication(Authentication auth) {
        return (IdentifiableUserDetails) auth.getPrincipal();
    }

    public static void setAuthExpiresAt(Map<String, Object> sessionAttributes, Instant expiresAt) {
        sessionAttributes.put("authExpiresAt", expiresAt);
    }

    public static Optional<String> extractBearerTokenFromStompHeader(StompHeaderAccessor accessor) {
        String authHeader = accessor.getFirstNativeHeader(HttpHeaders.AUTHORIZATION);

        if (authHeader == null || authHeader.isBlank() || !authHeader.startsWith(JwtService.BEARER_PREFIX)) {
            return Optional.empty();
        }

        String bearerToken = authHeader.substring(JwtService.BEARER_PREFIX.length());

        if (bearerToken.isBlank()) {
            return Optional.empty();
        }

        return Optional.of(bearerToken);
    }
}
