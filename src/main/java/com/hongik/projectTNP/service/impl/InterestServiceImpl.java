package com.hongik.projectTNP.service.impl;

import com.hongik.projectTNP.domain.Interest;
import com.hongik.projectTNP.domain.User;
import com.hongik.projectTNP.domain.UserInterest;
import com.hongik.projectTNP.dto.interest.InterestResponseDto;
import com.hongik.projectTNP.dto.interest.UserInterestRequestDto;
import com.hongik.projectTNP.exception.CustomException;
import com.hongik.projectTNP.repository.InterestRepository;
import com.hongik.projectTNP.repository.UserInterestRepository;
import com.hongik.projectTNP.repository.UserRepository;
import com.hongik.projectTNP.service.InterestService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InterestServiceImpl implements InterestService {

    private final InterestRepository interestRepository;
    private final UserInterestRepository userInterestRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public List<InterestResponseDto> getAllInterests() {
        return interestRepository.findAll().stream()
                .map(interest -> new InterestResponseDto(interest.getId(), interest.getName()))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void submitUserInterests(UserInterestRequestDto userInterestRequestDto, String userEmail) {
        User user = userRepository.findByUsername(userEmail)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다: " + userEmail));

        // 기존 사용자 관심사 삭제 (UserInterestRepository에 deleteByUser 메소드가 있다고 가정)
        List<UserInterest> existingUserInterests = userInterestRepository.findByUser(user);
        if (existingUserInterests != null && !existingUserInterests.isEmpty()) {
            userInterestRepository.deleteAll(existingUserInterests);
        }

        // 새로운 관심사 추가
        if (userInterestRequestDto.getInterestIds() != null && !userInterestRequestDto.getInterestIds().isEmpty()) {
            List<UserInterest> newUserInterests = userInterestRequestDto.getInterestIds().stream()
                    .map(interestId -> {
                        Interest interest = interestRepository.findById(interestId)
                                .orElseThrow(() -> new CustomException(HttpStatus.BAD_REQUEST, "존재하지 않는 관심사 ID입니다: " + interestId));
                        return UserInterest.builder()
                                .user(user)
                                .interest(interest)
                                .build();
                    })
                    .collect(Collectors.toList());
            userInterestRepository.saveAll(newUserInterests);
        }
    }
} 