package com.kvanzi.chatapp.user.dto;

import com.kvanzi.chatapp.user.enumeration.Role;
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
public class UserDTO {

    private String id;
    private String username;
    private Role role;
    private String firstName;
    private String lastName;
    private String avatarUrl;
    private String bio;
    private Instant createdAt;
    private Instant lastSeen;
    private UserStatus status;

    public UserStatus getStatus() {
        return status != null ? status : UserStatus.OFFLINE;
    }
}
