package com.hongik.projectTNP.controller;

import com.hongik.projectTNP.dto.auth.LoginRequestDto;
import com.hongik.projectTNP.dto.auth.SignupRequestDto;
import com.hongik.projectTNP.dto.auth.TokenResponseDto;
import com.hongik.projectTNP.dto.auth.UsernameCheckResponseDto;
import com.hongik.projectTNP.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.util.StringUtils;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/signup")
    public ResponseEntity<String> signup(@Valid @RequestBody SignupRequestDto signupRequestDto) {
        authService.signup(signupRequestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body("회원가입 성공");
    }

    @PostMapping("/login")
    public ResponseEntity<TokenResponseDto> login(@RequestBody LoginRequestDto loginRequestDto) {
        TokenResponseDto tokenResponseDto = authService.login(loginRequestDto);
        return ResponseEntity.ok(tokenResponseDto);
    }

    @GetMapping("/auth/check-username")
    public ResponseEntity<UsernameCheckResponseDto> checkUsername(@RequestParam String username) {
        if (!StringUtils.hasText(username)) {
            return ResponseEntity.badRequest().body(
                new UsernameCheckResponseDto(false, "아이디를 입력해주세요.")
            );
        }
        
        if (username.length() < 4 || username.length() > 20) {
            return ResponseEntity.badRequest().body(
                new UsernameCheckResponseDto(false, "아이디는 4~20자 사이여야 합니다.")
            );
        }
        
        boolean isAvailable = authService.checkUsernameAvailability(username);
        UsernameCheckResponseDto response = isAvailable ? 
            UsernameCheckResponseDto.available() : 
            UsernameCheckResponseDto.unavailable();
            
        return ResponseEntity.ok(response);
    }
} 