package com.kvanzi.chatapp.auth.component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kvanzi.chatapp.auth.exception.CustomAuthenticationException;
import com.kvanzi.chatapp.common.ResponseWrapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class AuthenticationEntryPoint implements org.springframework.security.web.AuthenticationEntryPoint {

    private final ObjectMapper objMapper;

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException)
            throws IOException, ServletException
    {
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        String message = authException instanceof CustomAuthenticationException
                ? authException.getMessage()
                : "Unauthorized";

        response.setHeader("Cache-Control", "no-store");
        response.setHeader("Pragma", "no-cache");

        response.getWriter().write(objMapper.writeValueAsString(
                ResponseWrapper.error(HttpStatus.UNAUTHORIZED, message)
        ));
        response.getWriter().flush();
    }
}
