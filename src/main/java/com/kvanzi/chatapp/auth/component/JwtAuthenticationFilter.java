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
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {


    private final AuthenticationEntryPoint authEntryPoint;
    private final AuthenticationManager authManager;
    private final JwtService jwtService;

    private final static Set<String> SHOULD_NOT_FILTER_PATTERNS = Set.of(
            "/api/v1/auth/**",
            "/error",
            "/ws",
            "/api/v1/users/@*",
            "/api/v1/users",
            "/favicon.ico"
    );
    private final static AntPathMatcher PATH_MATCHER = new AntPathMatcher();

    @Override
    protected boolean shouldNotFilter(@NonNull HttpServletRequest request) {
        String requestURI = request.getRequestURI();
        return SHOULD_NOT_FILTER_PATTERNS.stream()
                .anyMatch(pattern -> PATH_MATCHER.match(pattern, requestURI));
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
