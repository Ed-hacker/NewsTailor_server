package com.hongik.projectTNP.agent;

import com.hongik.projectTNP.domain.News;
import lombok.Getter;

@Getter
public class SummaryContext {
    private final News newsToSummarize; // 요약할 뉴스 원문 객체
    private final String articleContent; // 또는 단순히 요약할 텍스트 내용
    // 필요에 따라 요약 모델에 전달할 추가 파라미터 

    public SummaryContext(News newsToSummarize) {
        this.newsToSummarize = newsToSummarize;
        this.articleContent = newsToSummarize.getContent();
    }

    public SummaryContext(String articleContent) {
        this.newsToSummarize = null;
        this.articleContent = articleContent;
    }
    // 추가 생성자나 빌더 패턴도 가능
} 