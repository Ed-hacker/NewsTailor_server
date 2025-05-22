package com.hongik.projectTNP.service;

import com.hongik.projectTNP.domain.News;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 뉴스 관련 비즈니스 로직을 처리하는 서비스 인터페이스
 */
public interface NewsService {
    
    /**
     * 모든 뉴스를 조회합니다.
     * 
     * @return 뉴스 목록
     */
    List<News> findAll();
    
    /**
     * ID로 뉴스를 조회합니다.
     * 
     * @param id 뉴스 ID
     * @return 조회된 뉴스
     */
    News findById(Long id);
    
    /**
     * 특정 기간 내 발행된 뉴스를 조회합니다.
     * 
     * @param startDate 시작 날짜
     * @param endDate 종료 날짜
     * @return 해당 기간 내 뉴스 목록
     */
    List<News> findByDateRange(LocalDateTime startDate, LocalDateTime endDate);
    
    /**
     * 뉴스를 저장합니다.
     * 
     * @param news 저장할 뉴스 정보
     * @return 저장된 뉴스
     */
    News save(News news);
    
    /**
     * URL로 뉴스가 존재하는지 확인합니다.
     * 
     * @param url 확인할 URL
     * @return 존재 여부
     */
    boolean existsByUrl(String url);
    
} 