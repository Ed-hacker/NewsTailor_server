package com.hongik.projectTNP.service;

import com.hongik.projectTNP.domain.User;
import com.hongik.projectTNP.dto.user.ProfileUpdateDto;
import com.hongik.projectTNP.dto.user.UserInfoDto;

import java.util.Optional;

/**
 * 사용자 관련 비즈니스 로직을 처리하는 서비스 인터페이스
 */
public interface UserService {

    /**
     * 사용자명으로 사용자를 조회합니다.
     *
     * @param username 사용자명
     * @return 조회된 사용자 (Optional)
     */
    Optional<User> findByUsername(String username);

    User getCurrentUser(); // 현재 인증된 사용자 정보를 가져오는 메소드 (Spring Security Context 활용)

    /**
     * 사용자 정보를 조회합니다 (username, nickname, interests)
     *
     * @param username 사용자명
     * @return 사용자 정보 DTO
     */
    UserInfoDto getUserInfo(String username);

    /**
     * 프로필을 수정합니다 (닉네임, 관심분야)
     *
     * @param username 사용자명
     * @param profileUpdateDto 수정할 프로필 정보
     */
    void updateProfile(String username, ProfileUpdateDto profileUpdateDto);
} 