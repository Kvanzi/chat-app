package com.kvanzi.chatapp.ws.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.config.annotation.web.socket.EnableWebSocketSecurity;
import org.springframework.security.messaging.access.intercept.MessageMatcherDelegatingAuthorizationManager;

import static org.springframework.messaging.simp.SimpMessageType.*;

@Configuration
@EnableWebSocketSecurity
public class WebSocketSecurityConfig {

    @Bean
    AuthorizationManager<Message<?>> messageAuthorizationManager(MessageMatcherDelegatingAuthorizationManager.Builder messages) {
        return messages
                .nullDestMatcher().authenticated()
                .simpTypeMatchers(UNSUBSCRIBE, DISCONNECT).permitAll()
                .simpDestMatchers("/user/**", "/app/**").hasRole("USER")
                .simpSubscribeDestMatchers("/user/**").hasRole("USER")
                .anyMessage().denyAll()
                .build();
    }

    @Order(Ordered.HIGHEST_PRECEDENCE)
    @Bean("csrfChannelInterceptor")
    ChannelInterceptor noopCsrfChannelInterceptor() {
        return new ChannelInterceptor() {};
    }
}
