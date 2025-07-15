package com.kvanzi.chatapp.auth.component;

import com.kvanzi.chatapp.auth.exception.CustomAuthenticationException;
import com.kvanzi.chatapp.auth.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {


    private final AuthenticationEntryPoint authEntryPoint;
    private final AuthenticationManager authManager;
    private final JwtService jwtService;

    @Override
    protected boolean shouldNotFilter(@NonNull HttpServletRequest request) throws ServletException {
        String uri = request.getRequestURI();
        return uri.startsWith("/api/v1/auth")
                || uri.startsWith("/error")
                || uri.startsWith("/ws");
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain)
            throws ServletException, IOException {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth != null) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = jwtService.extractJWTToken(request);
        if (token == null) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            var jwtAuthToken = new JwtAuthenticationToken(token);
            jwtAuthToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            Authentication authenticatedToken = authManager.authenticate(jwtAuthToken);
            SecurityContextHolder.getContext().setAuthentication(authenticatedToken);
            filterChain.doFilter(request, response);
        } catch (BadCredentialsException e) {
            log.error("", e);
            handleAuthFailure(request, response, e.getMessage());
        } catch (RuntimeException e) {
            log.error("", e);
            handleAuthFailure(request, response, "Internal server error");
        }
    }

    private void handleAuthFailure(HttpServletRequest request, HttpServletResponse response, String message) throws ServletException, IOException {
        authEntryPoint.commence(request, response, new CustomAuthenticationException(message));
    }
}
