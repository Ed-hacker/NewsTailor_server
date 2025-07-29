package com.hongik.projectTNP.service;

import com.hongik.projectTNP.dto.news.NewsBriefResponseDto;
import com.hongik.projectTNP.dto.news.NewsDetailResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * 뉴스 관련 비즈니스 로직을 처리하는 서비스 인터페이스 (API 명세 기반)
 */
public interface NewsService {

    /**
     * 개인화된 뉴스 목록을 페이징하여 조회합니다.
     * @param userEmail 사용자 이메일
     * @param pageable 페이징 정보
     * @return 페이징된 뉴스 간략 정보 목록
     */
    Page<NewsBriefResponseDto> getPersonalizedNews(String userEmail, Pageable pageable);

    /**
     * 단일 뉴스 기사의 상세 정보를 조회합니다.
     * @param newsId 뉴스 ID
     * @param userEmail 사용자 이메일 (좋아요/북마크 여부 확인용)
     * @return 뉴스 상세 정보
     */
    NewsDetailResponseDto getNewsDetail(Long newsId, String userEmail);


    /**
     * 뉴스 기사를 '좋아요' 처리합니다.
     * @param newsId 뉴스 ID
     * @param userEmail 사용자 이메일
     */
    void likeArticle(Long newsId, String userEmail);

    /**
     * 뉴스 기사의 '좋아요'를 취소합니다.
     * @param newsId 뉴스 ID
     * @param userEmail 사용자 이메일
     */
    void unlikeArticle(Long newsId, String userEmail);

    /**
     * 뉴스 기사를 북마크합니다.
     * @param newsId 뉴스 ID
     * @param userEmail 사용자 이메일
     */
    void bookmarkArticle(Long newsId, String userEmail);

    /**
     * 뉴스 기사의 북마크를 취소합니다.
     * @param newsId 뉴스 ID
     * @param userEmail 사용자 이메일
     */
    void unbookmarkArticle(Long newsId, String userEmail);
} 