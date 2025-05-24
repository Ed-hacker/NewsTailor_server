package com.hongik.projectTNP.dto.news;

import com.hongik.projectTNP.domain.News;
import com.hongik.projectTNP.domain.Summary;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class NewsDetailResponseDto {
    private Long id;
    private String title;
    private String url;
    private String content; // 원문 내용
    private String category;
    private LocalDateTime publishedAt;
    private String summaryContent; // 요약 내용
    private boolean isLiked;       // 현재 사용자의 좋아요 여부
    private boolean isBookmarked;  // 현재 사용자의 북마크 여부
    private long likeCount;        // 전체 좋아요 수

    // Summary 객체를 선택적으로 받을 수 있도록 from 메소드를 여러 개 만들거나, 빌더를 활용
    public static NewsDetailResponseDto from(News news, Summary summary, boolean isLiked, boolean isBookmarked, long likeCount) {
        return NewsDetailResponseDto.builder()
                .id(news.getId())
                .title(news.getTitle())
                .url(news.getUrl())
                .content(news.getContent())
                .category(news.getCategory()) // News 엔티티에 category 필드 필요
                .publishedAt(news.getPublishedAt())
                .summaryContent(summary != null ? summary.getSummary_text() : null) // 변경: summary.getContent() -> summary.getSummary_text()
                .isLiked(isLiked)
                .isBookmarked(isBookmarked)
                .likeCount(likeCount)
                .build();
    }

    // 기존 from(News news, Summary summary)는 유지하거나 삭제 (API 명세에 따라 결정)
    // 우선 유지하고, 필요시 삭제 요청
    public static NewsDetailResponseDto from(News news, Summary summary) {
        return NewsDetailResponseDto.builder()
                .id(news.getId())
                .title(news.getTitle())
                .url(news.getUrl())
                .content(news.getContent())
                .category(news.getCategory())
                .publishedAt(news.getPublishedAt())
                .summaryContent(summary != null ? summary.getSummary_text() : null)
                // isLiked, isBookmarked, likeCount는 기본값 (false, 0) 또는 별도 처리 필요
                .isLiked(false) // 기본값
                .isBookmarked(false) // 기본값
                .likeCount(0L) // 기본값
                .build();
    }
} 