package com.kvanzi.chatapp.auth.component;

import lombok.Getter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

public class JwtAuthenticationToken extends AbstractAuthenticationToken {

    @Getter
    private final String token;
    @Getter
    private final Object principal;

    public JwtAuthenticationToken(String token, Object principal, Collection<? extends GrantedAuthority> authorities) {
        super(authorities);
        this.token = token;
        this.principal = principal;
        setAuthenticated(true);
    }

    public JwtAuthenticationToken(String token) {
        super(null);
        this.token = token;
        this.principal = null;
        setAuthenticated(false);
    }

    @Override
    public Object getCredentials() {
        return this.token;
    }
}
