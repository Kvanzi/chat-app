package com.kvanzi.chatapp.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kvanzi.chatapp.ws.decorator.CustomWebSocketHandlerDecorator;
import com.kvanzi.chatapp.ws.interceptor.AuthChannelInterceptor;
import com.kvanzi.chatapp.ws.interceptor.WebSocketChannelInterceptor;
import com.kvanzi.chatapp.ws.service.WebSocketSessionManager;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.messaging.converter.*;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketTransportRegistration;

import java.util.List;

@Configuration
@RequiredArgsConstructor
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final AuthenticationManager authManager;
    private final WebSocketSessionManager sessionManager;
    private final static String USER_DESTINATION_PREFIX = "/user/";

    @Override
    public void configureMessageBroker(@NonNull MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/user", "/topic");
        registry.setApplicationDestinationPrefixes("/app");
        registry.setUserDestinationPrefix(USER_DESTINATION_PREFIX);
    }

    @Override
    public void registerStompEndpoints(@NonNull StompEndpointRegistry registry) {
        registry.addEndpoint("/ws");
    }

    @Override
    public boolean configureMessageConverters(@NonNull List<MessageConverter> messageConverters) {
        var typeResolver = new DefaultContentTypeResolver();
        typeResolver.setDefaultMimeType(MimeTypeUtils.APPLICATION_JSON);

        var converter = new MappingJackson2MessageConverter();
        converter.setObjectMapper(new ObjectMapper());
        converter.setContentTypeResolver(typeResolver);

        messageConverters.add(new StringMessageConverter());
        messageConverters.add(new ByteArrayMessageConverter());
        messageConverters.add(converter);

        return false;
    }

    @Override
    public void configureClientInboundChannel(@NonNull ChannelRegistration registration) {
        registration.interceptors(
                new AuthChannelInterceptor(authManager),
                new WebSocketChannelInterceptor(USER_DESTINATION_PREFIX)
        );
    }

    @Override
    public void configureWebSocketTransport(@NonNull WebSocketTransportRegistration registry) {
        registry.addDecoratorFactory(handler ->
                new CustomWebSocketHandlerDecorator(handler, sessionManager)
        );
    }

    @Bean
    public TaskScheduler heartbeatTaskScheduler() {
        return new ThreadPoolTaskScheduler();
    }
}
