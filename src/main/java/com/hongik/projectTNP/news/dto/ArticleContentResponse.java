package com.hongik.projectTNP.news.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ArticleContentResponse {
    private Long id;
    private String title;
    private String url;
    private String body;
}