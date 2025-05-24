package com.hongik.projectTNP.service;

import com.hongik.projectTNP.dto.interest.InterestResponseDto;
import com.hongik.projectTNP.dto.interest.UserInterestRequestDto;

import java.util.List;

public interface InterestService {
    List<InterestResponseDto> getAllInterests();
    void submitUserInterests(UserInterestRequestDto userInterestRequestDto, String userEmail);
} 