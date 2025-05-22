package com.hongik.projectTNP.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 뉴스 요약 결과를 클라이언트에 전달하기 위한 DTO 클래스
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SummaryResponseDto {
    
    private Long id;              // 뉴스 ID
    private String title;         // 뉴스 제목
    private String url;           // 뉴스 원본 URL
    private String content;       // 뉴스 원본 내용 (선택)
    private String summaryText;   // 생성된 요약 텍스트
    private String audioUrl;      // 생성된 오디오 파일 URL
    private LocalDateTime publishedAt; // 뉴스 발행일
    
}
 