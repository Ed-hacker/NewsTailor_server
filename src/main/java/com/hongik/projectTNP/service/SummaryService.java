package com.hongik.projectTNP.service;

import com.hongik.projectTNP.domain.Summary;

public interface SummaryService {
    
    /**
     * 텍스트 내용을 기반으로 요약을 생성합니다.
     * 
     * @param content 요약할 텍스트 내용
     * @return 생성된 요약 텍스트
     */
    String generateSummary(String content);
    
    /**
     * 뉴스 ID에 해당하는 요약을 조회합니다.
     * 
     * @param newsId 뉴스 ID
     * @return 해당 뉴스의 요약 객체
     */
    Summary findByNewsId(Long newsId);
    
    /**
     * 요약 정보를 저장합니다.
     * 
     * @param summary 저장할 요약 객체
     * @return 저장된 요약 객체
     */
    Summary save(Summary summary);
} 