package com.hongik.projectTNP.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 로그인 성공 시 JWT 토큰을 반환하기 위한 DTO 클래스
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class TokenResponseDto {
    
    private String token;   // JWT 토큰
    private String name;    // 사용자 이름
    
} 