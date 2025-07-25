package com.kvanzi.chatapp.user.dto;

import com.kvanzi.chatapp.user.enumeration.UserStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserProfileDTO {

    private String firstName;
    private String lastName;
    private String avatarUrl;
    private String bio;
    private Instant lastSeen;
    private UserStatus status;
}
