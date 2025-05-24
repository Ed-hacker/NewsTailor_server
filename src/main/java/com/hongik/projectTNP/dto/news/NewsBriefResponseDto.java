package com.hongik.projectTNP.dto.news;

import com.hongik.projectTNP.domain.News;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class NewsBriefResponseDto {
    private Long id;
    private String title;
    private String category; // 뉴스 카테고리
    private LocalDateTime publishedAt;
    // 필요시 요약의 일부 또는 썸네일 URL 등 추가 가능

    public static NewsBriefResponseDto from(News news) {
        return NewsBriefResponseDto.builder()
                .id(news.getId())
                .title(news.getTitle())
                .category(news.getCategory()) // News 엔티티에 category 필드 필요
                .publishedAt(news.getPublishedAt())
                .build();
    }
} 