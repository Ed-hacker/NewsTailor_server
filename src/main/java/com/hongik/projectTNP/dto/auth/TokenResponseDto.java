package com.hongik.projectTNP.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class TokenResponseDto {
    private String accessToken;
    private String email;
    // 필요시 refreshToken도 추가 가능
} 