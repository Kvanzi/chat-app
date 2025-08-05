package com.kvanzi.chatapp.user.entity;

import com.kvanzi.chatapp.common.entity.BaseEntity;
import com.kvanzi.chatapp.user.component.IdentifiableUserDetails;
import com.kvanzi.chatapp.user.enumeration.Role;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@Entity
@Table(name = "users")
public class User extends BaseEntity implements IdentifiableUserDetails {

    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    private String passwordHash;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Role role;

    @Column
    private Instant lastPasswordChangedAt;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "profile_id")
    private UserProfile profile;

    public Optional<Instant> getLastPasswordChangedAtOpt() {
        return Optional.ofNullable(this.lastPasswordChangedAt);
    }

    @PrePersist
    public void prePersist() {
        setRole(Role.ROLE_USER);
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(role.name()));
    }

    @Override
    public String getPassword() {
        return this.passwordHash;
    }
}
