package com.hongik.projectTNP.controller;

import com.hongik.projectTNP.dto.auth.LoginRequestDto;
import com.hongik.projectTNP.dto.auth.SignupRequestDto;
import com.hongik.projectTNP.dto.auth.TokenResponseDto;
import com.hongik.projectTNP.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/signup")
    public ResponseEntity<String> signup(@RequestBody SignupRequestDto signupRequestDto) {
        authService.signup(signupRequestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body("회원가입 성공");
    }

    @PostMapping("/login")
    public ResponseEntity<TokenResponseDto> login(@RequestBody LoginRequestDto loginRequestDto) {
        TokenResponseDto tokenResponseDto = authService.login(loginRequestDto);
        return ResponseEntity.ok(tokenResponseDto);
    }
} 