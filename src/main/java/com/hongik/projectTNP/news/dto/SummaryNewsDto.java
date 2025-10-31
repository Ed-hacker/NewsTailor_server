package com.hongik.projectTNP.news.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SummaryNewsDto {

    private Integer sectionId;
    private String sectionName;
    private String title;
    private String url;
    private String summary;  // Gemini가 생성한 요약
}
