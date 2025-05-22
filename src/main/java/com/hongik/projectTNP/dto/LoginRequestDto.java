package com.hongik.projectTNP.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 로그인 요청을 위한 DTO 클래스
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequestDto {
    
    private String email;     // 사용자 이메일
    private String password;  // 사용자 비밀번호
    
} 