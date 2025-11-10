package com.hongik.projectTNP.news.service;

import com.hongik.projectTNP.news.dto.NewsCluster;
import com.hongik.projectTNP.news.dto.RawArticle;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class NewsClusteringService {

    /**
     * Phase 1: 키워드 추출 및 클러스터링
     */
    public List<NewsCluster> clusterNews(List<RawArticle> articles) {
        log.info("뉴스 클러스터링 시작 - 입력 뉴스 개수: {}", articles.size());

        // 1. 모든 뉴스에서 키워드 추출 및 빈도 계산
        Map<String, Integer> keywordFrequency = extractKeywordFrequency(articles);

        // 2. 빈도 높은 키워드만 선택 (2회 이상 등장)
        Set<String> significantKeywords = keywordFrequency.entrySet().stream()
                .filter(entry -> entry.getValue() >= 2)
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());

        log.info("주요 키워드 개수: {}", significantKeywords.size());

        // 3. 키워드별로 뉴스 그룹화
        Map<String, List<RawArticle>> keywordToArticles = new HashMap<>();
        for (RawArticle article : articles) {
            Set<String> articleKeywords = extractKeywords(article.getTitle());

            for (String keyword : articleKeywords) {
                if (significantKeywords.contains(keyword)) {
                    keywordToArticles.computeIfAbsent(keyword, k -> new ArrayList<>()).add(article);
                }
            }
        }

        // 4. 클러스터 생성 및 점수 계산
        List<NewsCluster> clusters = new ArrayList<>();
        for (Map.Entry<String, List<RawArticle>> entry : keywordToArticles.entrySet()) {
            String keyword = entry.getKey();
            List<RawArticle> clusterArticles = entry.getValue();

            // 언론사 다양성 계산
            Set<String> uniquePress = clusterArticles.stream()
                    .map(RawArticle::getPress)
                    .collect(Collectors.toSet());

            // 평균 순위 계산
            double averageRank = clusterArticles.stream()
                    .mapToInt(RawArticle::getRank)
                    .average()
                    .orElse(5.0);

            NewsCluster cluster = NewsCluster.builder()
                    .keyword(keyword)
                    .articles(clusterArticles)
                    .clusterSize(clusterArticles.size())
                    .pressDiversity(uniquePress.size())
                    .averageRank(averageRank)
                    .build();

            cluster.calculateScore();
            clusters.add(cluster);
        }

        // 5. 점수 순으로 정렬
        clusters.sort((c1, c2) -> Double.compare(c2.getClusterScore(), c1.getClusterScore()));

        log.info("클러스터 생성 완료 - 클러스터 개수: {}", clusters.size());
        return clusters;
    }

    /**
     * 모든 뉴스에서 키워드 추출 및 빈도 계산
     */
    private Map<String, Integer> extractKeywordFrequency(List<RawArticle> articles) {
        Map<String, Integer> frequency = new HashMap<>();

        for (RawArticle article : articles) {
            Set<String> keywords = extractKeywords(article.getTitle());
            for (String keyword : keywords) {
                frequency.put(keyword, frequency.getOrDefault(keyword, 0) + 1);
            }
        }

        return frequency;
    }

    /**
     * 제목에서 키워드 추출 (간단한 방식)
     * - 2글자 이상 단어
     * - 특수문자 제거
     * - 불용어 제거
     */
    private Set<String> extractKeywords(String title) {
        Set<String> keywords = new HashSet<>();

        // 불용어 리스트
        Set<String> stopWords = Set.of(
                "이", "그", "저", "것", "수", "등", "및", "또", "더", "를", "을", "가", "이", "은", "는",
                "에", "의", "와", "과", "도", "만", "까지", "부터", "으로", "로", "에서", "라고", "고",
                "한다", "했다", "한", "있다", "없다", "되다", "하다", "이다", "있는", "없는", "위해"
        );

        // 공백, 특수문자 기준으로 분리
        String[] tokens = title.split("[\\s\\[\\](){}\"',.\\/\\-]+");

        for (String token : tokens) {
            // 2글자 이상, 불용어가 아닌 경우만
            if (token.length() >= 2 && !stopWords.contains(token)) {
                keywords.add(token);
            }
        }

        return keywords;
    }

    /**
     * Phase 1: 상위 클러스터에서 대표 뉴스 선택
     */
    public List<RawArticle> selectCandidateNews(List<NewsCluster> clusters, int targetCount) {
        List<RawArticle> candidates = new ArrayList<>();
        Set<String> selectedUrls = new HashSet<>();

        // 상위 클러스터부터 순회
        for (NewsCluster cluster : clusters) {
            // 각 클러스터에서 최대 2~3개 선택
            int maxFromCluster = cluster.getClusterSize() >= 5 ? 3 : 2;
            int selectedFromCluster = 0;

            // 클러스터 내에서 순위 높은 순으로 선택
            List<RawArticle> sortedArticles = cluster.getArticles().stream()
                    .sorted(Comparator.comparingInt(RawArticle::getRank))
                    .collect(Collectors.toList());

            for (RawArticle article : sortedArticles) {
                if (selectedFromCluster >= maxFromCluster) break;
                if (selectedUrls.contains(article.getUrl())) continue;

                candidates.add(article);
                selectedUrls.add(article.getUrl());
                selectedFromCluster++;

                if (candidates.size() >= targetCount) {
                    log.info("Phase 1 완료 - 후보 뉴스 {}개 선택", candidates.size());
                    return candidates;
                }
            }
        }

        log.info("Phase 1 완료 - 후보 뉴스 {}개 선택", candidates.size());
        return candidates;
    }
}
