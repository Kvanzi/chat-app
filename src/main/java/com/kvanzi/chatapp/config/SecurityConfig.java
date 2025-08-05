package com.kvanzi.chatapp.config;

import com.kvanzi.chatapp.auth.component.JwtAuthenticationFilter;
import com.kvanzi.chatapp.auth.component.JwtAuthenticationProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, JwtAuthenticationProvider jwtAuthenticationProvider, JwtAuthenticationFilter authFilter, AuthenticationEntryPoint authEntryPoint) throws Exception {
        return http
                .cors(cors ->
                        cors.configurationSource(corsConfig())
                )
                .csrf(AbstractHttpConfigurer::disable)
                .authenticationProvider(jwtAuthenticationProvider)
                .authorizeHttpRequests(req -> req
                                .requestMatchers(
                                        "/api/v1/auth/**",
                                        "/error",
                                        "/api/v1/users/@*",
                                        "/api/v1/users",
                                        "/favicon.ico"
                                ).permitAll()
                                .anyRequest().authenticated()
                )
                .exceptionHandling(exceptionHandler -> exceptionHandler
                        .authenticationEntryPoint(authEntryPoint)
                )
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .addFilterBefore(
                        authFilter,
                        UsernamePasswordAuthenticationFilter.class
                )
                .build();
    }

    @Bean
    public AuthenticationProvider daoAuthProvider(UserDetailsService userDetailsService) {
        var provider = new DaoAuthenticationProvider(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public AuthenticationManager authManager(UserDetailsService userDetailsService, JwtAuthenticationProvider jwtAuthenticationProvider) {
        return new ProviderManager(List.of(
                jwtAuthenticationProvider,
                daoAuthProvider(userDetailsService)
        ));
    }

    @Bean
    public UrlBasedCorsConfigurationSource corsConfig() {
        var config = new CorsConfiguration();
        config.setAllowedOrigins(List.of(
                "http://localhost:63343",
                "http://localhost:5173" // vite
        ));
        config.setAllowedMethods(List.of("*"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);

        var source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return Argon2PasswordEncoder.defaultsForSpringSecurity_v5_8();
    }
}
