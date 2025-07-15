package com.kvanzi.chatapp.auth.component;

import com.kvanzi.chatapp.auth.enumeration.TokenType;
import com.kvanzi.chatapp.auth.exception.CustomAuthenticationException;
import com.kvanzi.chatapp.auth.exception.InvalidTokenTypeException;
import com.kvanzi.chatapp.auth.service.JwtService;
import com.kvanzi.chatapp.user.component.IdentifiableUserDetails;
import com.kvanzi.chatapp.user.service.UserDetailsService;
import com.kvanzi.chatapp.user.exception.UserNotFoundException;
import io.jsonwebtoken.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.time.Instant;

import static com.kvanzi.chatapp.auth.enumeration.TokenType.ACCESS;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationProvider implements AuthenticationProvider {

    private final UserDetailsService userDetailsService;
    private final JwtService jwtService;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        var authToken = (JwtAuthenticationToken) authentication;
        String token = authToken.getToken();

        try {
            Claims claims = jwtService.extractClaims(token);
            String userId = jwtService.extractUserId(claims);
            TokenType tokenType = jwtService.extractTokenType(claims);

            if (userId == null || tokenType == null) {
                throw new MalformedJwtException("Invalid token");
            }

            if (jwtService.extractExpiredAt(claims).isBefore(Instant.now())) {
                throw new BadCredentialsException("Token expired");
            }

            if (tokenType != ACCESS) {
                throw new InvalidTokenTypeException("You can't use '%s' token for access this resource".formatted(tokenType)); // FIXME: ADD CATCH HANDLER
            }

            IdentifiableUserDetails userDetails = userDetailsService.loadUserById(userId);
            return new JwtAuthenticationToken(
                    token,
                    userDetails,
                    userDetails.getAuthorities()
            );
        } catch (ExpiredJwtException e) {
            throw new BadCredentialsException("Access token expired");
        } catch (UnsupportedJwtException | MalformedJwtException e) {
            throw new BadCredentialsException("Invalid token. Re login or refresh your token");
        } catch (JwtException e) {
            throw new BadCredentialsException("Unexpected token validation exception. Re login or refresh your token");
        } catch (UserNotFoundException e) {
            throw new BadCredentialsException("Invalid token. Re login please");
        } catch (InvalidTokenTypeException e) {
            throw new BadCredentialsException(e.getMessage());
        } catch (RuntimeException e) {
            log.error("", e);
            throw new BadCredentialsException("Internal server error");
        }
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return JwtAuthenticationToken.class.isAssignableFrom(authentication);
    }
}
