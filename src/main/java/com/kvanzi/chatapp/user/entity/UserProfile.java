package com.kvanzi.chatapp.user.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.Objects;
import java.util.Optional;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "users_profiles")
public class UserProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false)
    private String firstName;

    @Column
    private String lastName;

    @Column
    private String avatarUrl;

    @Column
    private String bio;

    @Column
    private Instant lastSeen;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

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

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        UserProfile that = (UserProfile) o;
        return Objects.equals(getId(), that.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }
}
