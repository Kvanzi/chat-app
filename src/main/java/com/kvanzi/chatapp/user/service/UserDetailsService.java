package com.kvanzi.chatapp.user.service;

import com.kvanzi.chatapp.user.exception.UserNotFoundException;
import com.kvanzi.chatapp.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.owasp.encoder.Encode;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserDetailsService implements org.springframework.security.core.userdetails.UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByUsernameIgnoreCase(username)
                .orElseThrow(() -> new UserNotFoundException("User with username '%s' not found".formatted(Encode.forHtml(username))));
    }

    public UserDetails loadUserById(String id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User with id '%s' not found".formatted(Encode.forHtml(id))));
    }
}
