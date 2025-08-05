package com.kvanzi.chatapp.user.service;

import com.kvanzi.chatapp.user.component.IdentifiableUserDetails;
import com.kvanzi.chatapp.user.dto.UserDTO;
import com.kvanzi.chatapp.user.entity.User;
import com.kvanzi.chatapp.user.entity.UserProfile;
import com.kvanzi.chatapp.user.exception.UserNotFoundException;
import com.kvanzi.chatapp.user.exception.UsernameTakenException;
import com.kvanzi.chatapp.user.mapper.UserMapper;
import com.kvanzi.chatapp.user.repository.UserRepository;
import com.kvanzi.chatapp.ws.service.UserStatusService;
import lombok.RequiredArgsConstructor;
import org.owasp.encoder.Encode;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;
    private final UserStatusService statusService;

    @Transactional
    public User createUser(String username, String password, String firstName, String lastName, String bio) {
        userRepository.findByUsernameIgnoreCase(username).ifPresent(user -> {
            throw new UsernameTakenException("Username already taken");
        });

        if (bio != null && bio.isBlank()) {
            bio = null;
        }
        if (lastName != null && lastName.isBlank()) {
            lastName = null;
        }

        User user = User.builder()
                .username(username)
                .passwordHash(passwordEncoder.encode(password))
                .build();
        user = userRepository.save(user);

        UserProfile userProfile = UserProfile.builder()
                .firstName(firstName)
                .lastName(lastName)
                .bio(bio)
                .build();

        user.setProfile(userProfile);
        userProfile.setUser(user);

        return userRepository.save(user);
    }

    public User findById(String userId) {
        return userRepository.findById(userId)
                .orElseThrow(() ->
                        new UserNotFoundException("User with id '%s' not found"
                                .formatted(userId))
                );
    }

    public User findUserByUsername(String username) {
        return userRepository.findByUsernameIgnoreCase(username)
                .orElseThrow(() -> new UserNotFoundException(
                        "User with username '%s' not found".formatted(Encode.forHtml(username))
                ));
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public UserDTO userToDTO(User entity) {
        UserDTO dto = userMapper.userToDTO(entity);
        dto.getProfile().setStatus(statusService.getUserStatus(entity.getId()));
        return dto;
    }

    public IdentifiableUserDetails getCurrentUserDetails() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return null;
        }

        Object principal = auth.getPrincipal();

        if (principal instanceof IdentifiableUserDetails) {
            return (IdentifiableUserDetails) principal;
        }

        return null;
    }
}
