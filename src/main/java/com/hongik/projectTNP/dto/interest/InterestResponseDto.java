package com.hongik.projectTNP.dto.interest;

import com.hongik.projectTNP.domain.Interest;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class InterestResponseDto {
    private Long id;
    private String name;

    public static InterestResponseDto from(Interest interest) {
        return new InterestResponseDto(interest.getId(), interest.getName());
    }
} 