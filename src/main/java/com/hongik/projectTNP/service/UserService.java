package com.hongik.projectTNP.service;

import com.hongik.projectTNP.domain.User;

import java.util.Optional;

/**
 * 사용자 관련 비즈니스 로직을 처리하는 서비스 인터페이스
 */
public interface UserService {
    
    /**
     * 이메일로 사용자를 조회합니다.
     * 
     * @param email 사용자 이메일
     * @return 조회된 사용자 (Optional)
     */
    Optional<User> findByEmail(String email);
    
    /**
     * 이메일 중복 여부를 확인합니다.
     * 
     * @param email 확인할 이메일
     * @return 이메일 사용 가능 여부 (true: 이미 사용 중)
     */
    boolean existsByEmail(String email);
    
    /**
     * 사용자 정보를 저장합니다.
     * 
     * @param user 저장할 사용자 정보
     * @return 저장된 사용자 정보
     */
    User save(User user);
    
} 