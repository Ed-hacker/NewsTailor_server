package com.hongik.projectTNP.repository;

import com.hongik.projectTNP.domain.News;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface NewsRepository extends JpaRepository<News, Long> {
    
    /**
     * 특정 기간 내 발행된 뉴스 목록을 조회합니다.
     * 
     * @param startDate 시작 날짜
     * @param endDate 종료 날짜
     * @return 해당 기간 내 발행된 뉴스 목록
     */
    List<News> findByPublishedAtBetweenOrderByPublishedAtDesc(LocalDateTime startDate, LocalDateTime endDate);
    
    /**
     * URL로 뉴스가 존재하는지 확인합니다.
     * 
     * @param url 확인할 URL
     * @return 존재 여부
     */
    boolean existsByUrl(String url);
    
    Optional<News> findByUrl(String url);

    Page<News> findAllByOrderByPublishedAtDesc(Pageable pageable);

    // 사용자 관심사 기반 뉴스 목록 조회를 위한 메소드
    Page<News> findByCategoryInOrderByPublishedAtDesc(Set<String> categories, Pageable pageable);
} 