package com.kvanzi.chatapp.auth.component;

import com.kvanzi.chatapp.auth.exception.CustomAuthenticationException;
import com.kvanzi.chatapp.auth.service.JwtService;
import com.kvanzi.chatapp.user.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.lang.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private final static String BEARER_PREFIX = "Bearer ";

    private final UserRepository userRepository;
    private final AuthenticationEntryPoint authEntryPoint;
    private final JwtService jwtService;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain)
            throws ServletException, IOException
    {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth != null) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = extractAccessToken(request, response);
    }

    private String extractAccessToken(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException
    {
        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (authHeader == null || authHeader.startsWith(BEARER_PREFIX)) {
            authEntryPoint.commence(request, response, new CustomAuthenticationException("Access token is missing"));
            return null;
        }

        String token = authHeader.substring(BEARER_PREFIX.length()).trim();

        if (token.isEmpty()) {
            authEntryPoint.commence(request, response, new CustomAuthenticationException("Invalid access token"));
            return null;
        }

        return null; // FIXME
    }
}
