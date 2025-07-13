package com.kvanzi.chatapp.auth.service;

import com.kvanzi.chatapp.auth.dto.SignInRequestDTO;
import com.kvanzi.chatapp.auth.dto.SignUpRequestDTO;
import com.kvanzi.chatapp.user.entity.User;
import com.kvanzi.chatapp.user.exception.UserNotFoundException;
import com.kvanzi.chatapp.user.exception.UsernameTakenException;
import com.kvanzi.chatapp.user.repository.UserRepository;
import com.kvanzi.chatapp.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.owasp.encoder.Encode;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserService userService;
    private final AuthenticationManager authManager;

    public User signUp(SignUpRequestDTO requestDTO) {
        return signUp(
                requestDTO.getUsername(),
                requestDTO.getPassword(),
                requestDTO.getFirstName(),
                requestDTO.getLastName(),
                requestDTO.getBio()
        );
    }

    public User signUp(String username, String password, String firstName, String lastName, String bio) {
        userRepository.findByUsernameIgnoreCase(username).ifPresent(user -> {
            throw new UsernameTakenException("Username already taken");
        });

        User user = User.builder()
                .username(username)
                .passwordHash(passwordEncoder.encode(password))
                .firstName(firstName)
                .lastName(lastName)
                .bio(bio)
                .build();

        return userService.createUser(user);
    }

    public User signIn(SignInRequestDTO requestDTO) {
        return signIn(requestDTO.getUsername(), requestDTO.getPassword());
    }

    public User signIn(String username, String password) {
        User user = userRepository.findByUsernameIgnoreCase(username)
                .orElseThrow(() -> new UserNotFoundException("User with username '%s' not found".formatted(Encode.forHtml(username))));

        authManager.authenticate(new UsernamePasswordAuthenticationToken(
                username,
                password
        ));

        return user;
    }
}
