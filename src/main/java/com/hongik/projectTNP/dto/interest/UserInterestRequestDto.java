package com.hongik.projectTNP.dto.interest;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class UserInterestRequestDto {
    private List<Long> interestIds; // 선택한 관심사 ID 목록
} 