package com.kvanzi.chatapp.auth.controller;

import com.kvanzi.chatapp.auth.dto.SignInRequestDTO;
import com.kvanzi.chatapp.auth.dto.SignUpRequestDTO;
import com.kvanzi.chatapp.auth.service.AuthService;
import com.kvanzi.chatapp.auth.service.JwtService;
import com.kvanzi.chatapp.common.ResponseWrapper;
import com.kvanzi.chatapp.user.dto.UserDTO;
import com.kvanzi.chatapp.user.entity.User;
import com.kvanzi.chatapp.user.mapper.UserMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;
    private final UserMapper userMapper;
    private final JwtService jwtService;

    @PostMapping("/sign-up")
    public ResponseEntity<ResponseWrapper<UserDTO>> signUp(
            @Valid @RequestBody SignUpRequestDTO requestDTO
    ) {
        User user = authService.signUp(requestDTO);

        return ResponseWrapper.okEntity(
                "User created successfully",
                userMapper.toDTO(user)
        );
    }

    @PostMapping("/sign-in")
    public ResponseEntity<ResponseWrapper<UserDTO>> signIn(
            @Valid @RequestBody SignInRequestDTO requestDTO
    ) {
        User user = authService.signIn(requestDTO);

        return ResponseWrapper.success(
                HttpStatus.OK,
                "User logged in successfully",
                userMapper.toDTO(user)
        )
                .addField("accessToken", jwtService.generateAccessToken(user.getId()))
                .addField("refreshToken", jwtService.generateRefreshToken(user.getId()))
                .toResponseEntity();
    }

    @PostMapping("/refresh")
    public ResponseEntity<ResponseWrapper<String>> refreshAccessToken(@NonNull HttpServletRequest request) {
        String refreshedToken = jwtService.refreshAccessToken(request);
        return ResponseWrapper.okEntity(refreshedToken);
    }
}
