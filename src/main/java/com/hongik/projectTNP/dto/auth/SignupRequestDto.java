package com.hongik.projectTNP.dto.auth;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SignupRequestDto {
    private String email;
    private String password;
    private String nickname;
    // 추가적인 회원가입 정보 필드 (예: email, nickname 등)가 필요하면 여기에 추가
} 