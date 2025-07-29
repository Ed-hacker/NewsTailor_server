package com.hongik.projectTNP.service;

import com.hongik.projectTNP.dto.auth.LoginRequestDto;
import com.hongik.projectTNP.dto.auth.SignupRequestDto;
import com.hongik.projectTNP.dto.auth.TokenResponseDto;

public interface AuthService {
    void signup(SignupRequestDto signupRequestDto);
    TokenResponseDto login(LoginRequestDto loginRequestDto);
    boolean checkUsernameAvailability(String username);
} 