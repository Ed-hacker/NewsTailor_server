package com.hongik.projectTNP.service;

import com.hongik.projectTNP.domain.User;

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
} 