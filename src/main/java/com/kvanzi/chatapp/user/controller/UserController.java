package com.kvanzi.chatapp.user.controller;

import com.kvanzi.chatapp.common.ResponseWrapper;
import com.kvanzi.chatapp.user.dto.UserDTO;
import com.kvanzi.chatapp.user.entity.User;
import com.kvanzi.chatapp.user.mapper.UserMapper;
import com.kvanzi.chatapp.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;
    private final UserMapper userMapper;

    @GetMapping("/me")
    public ResponseEntity<ResponseWrapper<UserDTO>> me(@AuthenticationPrincipal User user) {
        return ResponseWrapper.okEntity(
                userMapper.toDTO(user)
        );
    }

    @GetMapping("/@{username}")
    public ResponseEntity<ResponseWrapper<UserDTO>> me(@PathVariable String username) {
        User user = userService.findUserByUsername(username);
        return ResponseWrapper.okEntity(
                userMapper.toDTO(user)
        );
    }
}
