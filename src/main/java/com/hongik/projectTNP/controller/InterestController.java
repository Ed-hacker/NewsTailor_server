package com.hongik.projectTNP.controller;

import com.hongik.projectTNP.dto.interest.InterestResponseDto;
import com.hongik.projectTNP.dto.interest.UserInterestRequestDto;
import com.hongik.projectTNP.service.InterestService;
import com.hongik.projectTNP.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class InterestController {

    private final InterestService interestService;
    private final UserService userService; // 사용자 정보 확인용

    @GetMapping("/interests")
    public ResponseEntity<List<InterestResponseDto>> getAllInterests() {
        List<InterestResponseDto> interests = interestService.getAllInterests();
        return ResponseEntity.ok(interests);
    }

    @PostMapping("/user/interests")
    public ResponseEntity<String> submitUserInterests(@AuthenticationPrincipal UserDetails userDetails,
                                                      @RequestBody UserInterestRequestDto userInterestRequestDto) {
        // UserDetails에서 사용자 이메일(username)을 가져옵니다.
        String userEmail = userDetails.getUsername();
        interestService.submitUserInterests(userInterestRequestDto, userEmail);
        return ResponseEntity.ok("관심사 등록 성공");
    }
} 