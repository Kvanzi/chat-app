package com.kvanzi.chatapp.user.service;

import com.kvanzi.chatapp.user.entity.User;
import com.kvanzi.chatapp.user.exception.UserNotFoundException;
import com.kvanzi.chatapp.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.owasp.encoder.Encode;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public User createUser(User user) {
        if (user.getBio() != null && user.getBio().isBlank()) {
            user.setBio(null);
        }
        if (user.getLastName() != null && user.getLastName().isBlank()) {
            user.setLastName(null);
        }
        return userRepository.save(user);
    }

    public User findUserByUsername(String username) {
        return userRepository.findByUsernameIgnoreCase(username)
                .orElseThrow(() -> new UserNotFoundException(
                        "User with username '%s' not found".formatted(Encode.forHtml(username))
                ));
    }
}
