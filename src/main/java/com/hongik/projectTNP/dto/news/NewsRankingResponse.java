package com.hongik.projectTNP.dto.news;

import com.hongik.projectTNP.domain.NewsRanking;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NewsRankingResponse {
    private Long id;
    private Integer sectionId;
    private String press;
    private Integer rank;
    private String title;
    private String url;
    private String summary;  // Gemini가 생성한 요약 (본문은 제외)
    private LocalDateTime collectedAt;

    public static NewsRankingResponse from(NewsRanking newsRanking) {
        return NewsRankingResponse.builder()
                .id(newsRanking.getId())
                .sectionId(newsRanking.getSectionId())
                .press(newsRanking.getPress())
                .rank(newsRanking.getRank())
                .title(newsRanking.getTitle())
                .url(newsRanking.getUrl())
                .summary(newsRanking.getSummary())
                .collectedAt(newsRanking.getCollectedAt())
                .build();
    }
}