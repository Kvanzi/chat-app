package com.kvanzi.chatapp.user.entity;

import com.kvanzi.chatapp.user.enumeration.Role;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "users")
public class User implements UserDetails {

    // FIXME: split into 2 parts user and user profile

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    private String passwordHash;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Role role;

    @Column(nullable = false)
    private String firstName;

    @Column
    private String lastName;

    @Column
    private String avatarUrl;

    @Column
    private String bio;

    @Column(nullable = false)
    private Instant createdAt;

    @Column
    private Instant lastSeen;

    @Column
    private Instant lastPasswordChangedAt;

    public Optional<Instant> getLastPasswordChangedAtOpt() {
        return Optional.ofNullable(this.lastPasswordChangedAt);
    }

    public Optional<Instant> getLastSeenOpt() {
        return Optional.ofNullable(this.lastSeen);
    }

    public Optional<String> getLastNameOpt() {
        return Optional.ofNullable(this.lastName);
    }

    public Optional<String> getBioOpt() {
        return Optional.ofNullable(this.bio);
    }

    public Optional<String> getAvatarUrlOpt() {
        return Optional.ofNullable(this.avatarUrl);
    }

    @PrePersist
    public void prePersist() {
        setCreatedAt(Instant.now());
        setRole(Role.USER);
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(role.name()));
    }

    @Override
    public String getPassword() {
        return this.passwordHash;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(getId(), user.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }
}
