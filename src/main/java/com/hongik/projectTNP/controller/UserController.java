package com.hongik.projectTNP.controller;

import com.hongik.projectTNP.dto.user.ProfileUpdateDto;
import com.hongik.projectTNP.dto.user.UserInfoDto;
import com.hongik.projectTNP.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * 로그인한 사용자의 정보 조회
     * GET /api/user/info
     */
    @GetMapping("/info")
    public ResponseEntity<UserInfoDto> getUserInfo(Authentication authentication) {
        String username = authentication.getName();
        UserInfoDto userInfo = userService.getUserInfo(username);
        return ResponseEntity.ok(userInfo);
    }

    /**
     * 프로필 수정 (닉네임, 관심분야)
     * PUT /api/user/profile
     */
    @PutMapping("/profile")
    public ResponseEntity<String> updateProfile(
            Authentication authentication,
            @Valid @RequestBody ProfileUpdateDto profileUpdateDto) {
        String username = authentication.getName();
        userService.updateProfile(username, profileUpdateDto);
        return ResponseEntity.ok("프로필이 수정되었습니다.");
    }
}
