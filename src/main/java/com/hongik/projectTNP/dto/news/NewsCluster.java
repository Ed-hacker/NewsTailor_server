package com.hongik.projectTNP.dto.news;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NewsCluster {

    private String keyword;              // 대표 키워드
    private List<RawArticle> articles;   // 클러스터에 속한 뉴스들
    private int clusterSize;             // 클러스터 크기 (뉴스 개수)
    private int pressDiversity;          // 언론사 다양성 (몇 개 언론사)
    private double averageRank;          // 평균 언론사 순위
    private double clusterScore;         // 클러스터 점수

    /**
     * 클러스터 점수 계산
     */
    public void calculateScore() {
        // 점수 = (클러스터 크기 × 3) + (언론사 다양성 × 2) + (평균 순위 점수)
        double rankScore = (6.0 - averageRank);  // 1위=5점, 2위=4점, ...
        this.clusterScore = (clusterSize * 3.0) + (pressDiversity * 2.0) + rankScore;
    }
}
