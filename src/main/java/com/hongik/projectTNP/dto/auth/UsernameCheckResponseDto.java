package com.hongik.projectTNP.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UsernameCheckResponseDto {
    private boolean available;
    private String message;
    
    public static UsernameCheckResponseDto available() {
        return new UsernameCheckResponseDto(true, "사용 가능한 아이디입니다.");
    }
    
    public static UsernameCheckResponseDto unavailable() {
        return new UsernameCheckResponseDto(false, "이미 사용 중인 아이디입니다.");
    }
}