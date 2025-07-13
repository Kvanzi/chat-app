package com.kvanzi.chatapp.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SignUpRequestDTO {

    @NotBlank(message = "Username cannot be blank")
    @Size(min = 3, max = 20, message = "Username must be between 3 and 20 characters")
    @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "Username can only contain letters, digits and underscores")
    private String username;

    @NotBlank(message = "Password cannot be blank")
    @Size(min = 8, max = 100, message = "Password must be between 8 and 100 characters")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]+$",
            message = "Password must contain at least one lowercase letter, one uppercase letter, one digit and one special character")
    private String password;

    @NotBlank(message = "First name cannot be blank")
    @Size(min = 2, max = 20, message = "First name must be between 2 and 20 characters")
    @Pattern(regexp = "^[\\p{L}\\p{M}\\s-']+$", message = "First name can only contain letters, spaces, hyphens and apostrophes")
    private String firstName;

    @Size(min = 2, max = 20, message = "Last name must be between 2 and 20 characters")
    @Pattern(regexp = "^[\\p{L}\\p{M}\\s-']+$", message = "Last name can only contain letters, spaces, hyphens and apostrophes")
    private String lastName;

    @Size(max = 500, message = "Bio cannot exceed 500 characters")
    private String bio;
}