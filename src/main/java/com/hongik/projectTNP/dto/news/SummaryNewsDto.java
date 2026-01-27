package com.hongik.projectTNP.dto.news;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SummaryNewsDto {

    private Long id;  // SummaryNewsCache ID (북마크용)
    private Integer sectionId;
    private String sectionName;
    private String title;
    private String url;
    private String summary;  // Gemini가 생성한 요약
    private Integer rankOrder;  // 1~4
}
