package com.hongik.projectTNP.dto.news;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RawArticle {
    private Integer sectionId;
    private String press;
    private Integer rank;
    private String title;
    private String url;
    private String body;
}