package com.kvanzi.chatapp.user.controller;

import com.kvanzi.chatapp.common.ResponseWrapper;
import com.kvanzi.chatapp.user.dto.UserDTO;
import com.kvanzi.chatapp.user.entity.User;
import com.kvanzi.chatapp.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<ResponseWrapper<UserDTO>> me(@AuthenticationPrincipal User user) {
        return ResponseWrapper.okEntity(
                userService.userToDTO(user)
        );
    }

    @GetMapping("/@{username}")
    public ResponseEntity<ResponseWrapper<UserDTO>> me(@PathVariable String username) {
        User user = userService.findUserByUsername(username);
        return ResponseWrapper.okEntity(
                userService.userToDTO(user)
        );
    }

    @GetMapping
    public ResponseEntity<ResponseWrapper<List<UserDTO>>> getAllUsers() {
        return ResponseWrapper.okEntity(
                userService.getAllUsers()
                        .stream()
                        .map(userService::userToDTO)
                        .toList()
        );
    }
}
