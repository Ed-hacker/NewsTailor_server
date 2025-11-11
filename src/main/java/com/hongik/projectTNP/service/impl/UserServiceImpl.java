package com.hongik.projectTNP.service.impl;

import com.hongik.projectTNP.domain.Interest;
import com.hongik.projectTNP.domain.User;
import com.hongik.projectTNP.domain.UserInterest;
import com.hongik.projectTNP.dto.user.ProfileUpdateDto;
import com.hongik.projectTNP.dto.user.UserInfoDto;
import com.hongik.projectTNP.repository.InterestRepository;
import com.hongik.projectTNP.repository.UserInterestRepository;
import com.hongik.projectTNP.repository.UserRepository;
import com.hongik.projectTNP.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserInterestRepository userInterestRepository;
    private final InterestRepository interestRepository;

    @Override
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    @Override
    public User getCurrentUser() {
        // Spring Security 컨텍스트에서 현재 사용자 정보 가져오기 (추후 SecurityConfig 설정 필요)
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + username));
    }

    @Override
    @Transactional(readOnly = true)
    public UserInfoDto getUserInfo(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + username));

        List<String> interests = userInterestRepository.findByUser(user)
                .stream()
                .map(userInterest -> userInterest.getInterest().getName())
                .collect(Collectors.toList());

        return UserInfoDto.builder()
                .username(user.getUsername())
                .nickname(user.getNickname())
                .interests(interests)
                .build();
    }

    @Override
    @Transactional
    public void updateProfile(String username, ProfileUpdateDto profileUpdateDto) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + username));

        // 1. 닉네임 업데이트
        user.updateNickname(profileUpdateDto.getNickname());
        userRepository.save(user);  // 명시적으로 저장

        // 2. 기존 관심분야 삭제
        List<UserInterest> existingInterests = userInterestRepository.findByUser(user);
        userInterestRepository.deleteAll(existingInterests);

        // 3. 새로운 관심분야 추가
        List<Interest> interests = interestRepository.findAllById(profileUpdateDto.getInterestIds());

        if (interests.size() != 3) {
            throw new IllegalArgumentException("정확히 3개의 관심분야를 선택해야 합니다.");
        }

        List<UserInterest> newUserInterests = interests.stream()
                .map(interest -> UserInterest.builder()
                        .user(user)
                        .interest(interest)
                        .build())
                .collect(Collectors.toList());

        userInterestRepository.saveAll(newUserInterests);
    }
} 