package com.hongik.projectTNP.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 커스텀 뉴스 처리 요청을 위한 DTO 클래스
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NewsRequestDto {
    
    private String title;       // 뉴스 제목
    private String content;     // 뉴스 내용
    private String url;         // 뉴스 원본 URL (선택)
    
} 