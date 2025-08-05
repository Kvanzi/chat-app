package com.kvanzi.chatapp.user.entity;

import com.kvanzi.chatapp.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.Instant;
import java.util.Optional;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@Entity
@Table(name = "users_profiles")
public class UserProfile extends BaseEntity {

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
}
