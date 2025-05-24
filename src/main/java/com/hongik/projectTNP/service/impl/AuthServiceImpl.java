package com.hongik.projectTNP.service.impl;

import com.hongik.projectTNP.domain.User;
import com.hongik.projectTNP.dto.auth.LoginRequestDto;
import com.hongik.projectTNP.dto.auth.SignupRequestDto;
import com.hongik.projectTNP.dto.auth.TokenResponseDto;
import com.hongik.projectTNP.exception.CustomException;
import com.hongik.projectTNP.repository.UserRepository;
import com.hongik.projectTNP.service.AuthService;
import com.hongik.projectTNP.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Override
    @Transactional
    public void signup(SignupRequestDto signupRequestDto) {
        if (userRepository.existsByEmail(signupRequestDto.getEmail())) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "이미 사용 중인 이메일입니다.");
        }

        String encodedPassword = passwordEncoder.encode(signupRequestDto.getPassword());

        User user = User.builder()
                .email(signupRequestDto.getEmail())
                .password(encodedPassword)
                .nickname(signupRequestDto.getNickname())
                .build();
        userRepository.save(user);
    }

    @Override
    @Transactional(readOnly = true)
    public TokenResponseDto login(LoginRequestDto loginRequestDto) {
        User user = userRepository.findByEmail(loginRequestDto.getEmail())
                .orElseThrow(() -> new CustomException(HttpStatus.UNAUTHORIZED, "이메일 또는 비밀번호가 일치하지 않습니다."));

        if (!passwordEncoder.matches(loginRequestDto.getPassword(), user.getPassword())) {
            throw new CustomException(HttpStatus.UNAUTHORIZED, "이메일 또는 비밀번호가 일치하지 않습니다.");
        }

        String token = jwtUtil.generateToken(user.getEmail());
        return new TokenResponseDto(token, user.getEmail());
    }
} 