package com.hongik.projectTNP.service.impl;

import com.hongik.projectTNP.domain.Interest;
import com.hongik.projectTNP.domain.User;
import com.hongik.projectTNP.domain.UserInterest;
import com.hongik.projectTNP.dto.auth.LoginRequestDto;
import com.hongik.projectTNP.dto.auth.SignupRequestDto;
import com.hongik.projectTNP.dto.auth.TokenResponseDto;
import com.hongik.projectTNP.exception.CustomException;
import com.hongik.projectTNP.repository.InterestRepository;
import com.hongik.projectTNP.repository.UserInterestRepository;
import com.hongik.projectTNP.repository.UserRepository;
import com.hongik.projectTNP.service.AuthService;
import com.hongik.projectTNP.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final InterestRepository interestRepository;
    private final UserInterestRepository userInterestRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Override
    @Transactional
    public void signup(SignupRequestDto signupRequestDto) {
        if (userRepository.existsByUsername(signupRequestDto.getUsername())) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "이미 사용 중인 아이디입니다.");
        }
        
        String encodedPassword = passwordEncoder.encode(signupRequestDto.getPassword());

        User user = User.builder()
                .username(signupRequestDto.getUsername())
                .password(encodedPassword)
                .nickname(signupRequestDto.getNickname())
                .build();
        User savedUser = userRepository.save(user);
        
        // 관심사 설정
        if (signupRequestDto.getInterestIds() != null && !signupRequestDto.getInterestIds().isEmpty()) {
            List<UserInterest> userInterests = signupRequestDto.getInterestIds().stream()
                    .map(interestId -> {
                        Interest interest = interestRepository.findById(interestId)
                                .orElseThrow(() -> new CustomException(HttpStatus.BAD_REQUEST, 
                                        "존재하지 않는 관심사 ID입니다: " + interestId));
                        return UserInterest.builder()
                                .user(savedUser)
                                .interest(interest)
                                .build();
                    })
                    .collect(Collectors.toList());
            userInterestRepository.saveAll(userInterests);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public TokenResponseDto login(LoginRequestDto loginRequestDto) {
        User user = userRepository.findByUsername(loginRequestDto.getUsername())
                .orElseThrow(() -> new CustomException(HttpStatus.UNAUTHORIZED, "아이디 또는 비밀번호가 일치하지 않습니다."));

        if (!passwordEncoder.matches(loginRequestDto.getPassword(), user.getPassword())) {
            throw new CustomException(HttpStatus.UNAUTHORIZED, "아이디 또는 비밀번호가 일치하지 않습니다.");
        }

        String token = jwtUtil.generateToken(user.getUsername());
        return new TokenResponseDto(token, user.getUsername());
    }

    @Override
    @Transactional(readOnly = true)
    public boolean checkUsernameAvailability(String username) {
        return !userRepository.existsByUsername(username);
    }
} 