package com.hongik.projectTNP.repository;

import com.hongik.projectTNP.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    /**
     * 사용자명(아이디)으로 사용자를 조회합니다.
     * 
     * @param username 조회할 사용자명
     * @return 해당 사용자명을 가진 사용자 (Optional)
     */
    Optional<User> findByUsername(String username);
    
    /**
     * 해당 사용자명이 존재하는지 확인합니다.
     * 
     * @param username 확인할 사용자명
     * @return 사용자명 존재 여부
     */
    boolean existsByUsername(String username);
    
    /**
     * 이메일로 사용자를 조회합니다.
     * 
     * @param email 조회할 이메일
     * @return 해당 이메일을 가진 사용자 (Optional)
     */
    Optional<User> findByEmail(String email);
    
    /**
     * 해당 이메일을 가진 사용자가 존재하는지 확인합니다.
     * 
     * @param email 확인할 이메일
     * @return 이메일 존재 여부
     */
    boolean existsByEmail(String email);
    
} 