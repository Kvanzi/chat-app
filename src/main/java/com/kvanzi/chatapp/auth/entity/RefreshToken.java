package com.kvanzi.chatapp.auth.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.time.Instant;
import java.util.Objects;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "refresh_tokens")
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(name = "is_revoked", nullable = false)
    private Boolean isRevoked;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "issued_at", nullable = false)
    private Instant issuedAt;

    @Column(name = "token_hash")
    private String tokenHash;

    @Column(name = "user_id")
    private String userId;

    public boolean isTokenMatches(BCryptPasswordEncoder encoder, String token) {
        if (getTokenHash() == null || token == null) {
            return false;
        }
        return encoder.matches(token, getTokenHash());
    }

    @PrePersist
    public void prePersist() {
        if (isRevoked == null) {
            setIsRevoked(false);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        RefreshToken that = (RefreshToken) o;
        return Objects.equals(getId(), that.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }
}
