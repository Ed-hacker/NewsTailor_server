package com.hongik.projectTNP.repository;

import com.hongik.projectTNP.domain.Interest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface InterestRepository extends JpaRepository<Interest, Long> {
    Optional<Interest> findByName(String name); // 이름으로 관심사를 찾는 메소드
} 