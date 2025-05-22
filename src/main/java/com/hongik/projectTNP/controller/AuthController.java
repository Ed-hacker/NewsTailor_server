package com.hongik.projectTNP.controller;

import com.hongik.projectTNP.domain.User;
import com.hongik.projectTNP.dto.LoginRequestDto;
import com.hongik.projectTNP.dto.SignupRequestDto;
import com.hongik.projectTNP.dto.TokenResponseDto;
import com.hongik.projectTNP.service.UserService;
import com.hongik.projectTNP.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Autowired
    public AuthController(UserService userService, PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/signup")
    public ResponseEntity<String> signup(@RequestBody SignupRequestDto signupRequestDto) {
        // 이메일 중복 검사
        if (userService.existsByEmail(signupRequestDto.getEmail())) {
            return ResponseEntity.badRequest().body("이미 사용 중인 이메일입니다.");
        }

        // 비밀번호 암호화
        String encodedPassword = passwordEncoder.encode(signupRequestDto.getPassword());

        // 사용자 생성
        User user = User.builder()
                .email(signupRequestDto.getEmail())
                .password(encodedPassword)
                .name(signupRequestDto.getName())
                .build();

        userService.save(user);
        return ResponseEntity.ok("회원가입이 완료되었습니다.");
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequestDto loginRequestDto) {
        // 사용자 검증
        User user = userService.findByEmail(loginRequestDto.getEmail())
                .orElse(null);

        if (user == null || !passwordEncoder.matches(loginRequestDto.getPassword(), user.getPassword())) {
            return ResponseEntity.badRequest().body("이메일 또는 비밀번호가 일치하지 않습니다.");
        }

        // JWT 토큰 생성
        String token = jwtUtil.generateToken(user.getEmail());
        return ResponseEntity.ok(new TokenResponseDto(token, user.getName()));
    }
} 