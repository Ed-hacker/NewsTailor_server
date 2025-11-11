package com.hongik.projectTNP.news.service;

import com.hongik.projectTNP.news.dto.NewsCluster;
import com.hongik.projectTNP.news.dto.RawArticle;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
@RequiredArgsConstructor
public class NewsRankingSelectionService {

    private final NewsClusteringService newsClusteringService;
    private final RestTemplate restTemplate;

    @Value("${gemini.api.key}")
    private String geminiApiKey;

    @Value("${gemini.api.url}")
    private String geminiApiUrl;

    /**
     * 하이브리드 랭킹 알고리즘: Phase 1 + Phase 2
     */
    public List<RawArticle> selectTop20News(List<RawArticle> allRankingNews) {
        log.info("===== 하이브리드 랭킹 알고리즘 시작 =====");
        log.info("입력 뉴스 개수: {}", allRankingNews.size());

        // Phase 1: 클러스터링 및 1차 필터링
        List<NewsCluster> clusters = newsClusteringService.clusterNews(allRankingNews);
        List<RawArticle> candidates = newsClusteringService.selectCandidateNews(clusters, 30);

        log.info("Phase 1 완료 - 후보 뉴스: {}개", candidates.size());

        // Phase 2: Gemini 기반 최종 선정
        List<RawArticle> finalTop20 = selectFinalTop20WithGemini(candidates, clusters);

        log.info("===== 하이브리드 랭킹 알고리즘 완료 - 최종 {}개 선정 =====", finalTop20.size());
        return finalTop20;
    }

    /**
     * Phase 2: Gemini를 활용한 최종 20개 선정
     */
    private List<RawArticle> selectFinalTop20WithGemini(List<RawArticle> candidates, List<NewsCluster> clusters) {
        log.info("Phase 2: Gemini 기반 최종 선정 시작");

        // 클러스터 정보를 포함한 프롬프트 생성
        String prompt = buildGeminiPrompt(candidates, clusters);

        // Gemini API 호출
        String geminiResponse = callGeminiApi(prompt);

        if (geminiResponse == null || geminiResponse.isEmpty()) {
            log.warn("Gemini 응답 없음 - Phase 1 결과 상위 20개 반환");
            return candidates.subList(0, Math.min(20, candidates.size()));
        }

        // Gemini 응답 파싱
        List<RawArticle> selectedNews = parseGeminiResponse(geminiResponse, candidates);

        log.info("Phase 2 완료 - Gemini 선정 뉴스: {}개", selectedNews.size());
        return selectedNews;
    }

    /**
     * Gemini 프롬프트 생성
     */
    private String buildGeminiPrompt(List<RawArticle> candidates, List<NewsCluster> clusters) {
        StringBuilder prompt = new StringBuilder();

        prompt.append("다음은 클러스터링된 뉴스 목록입니다. 각 뉴스는 클러스터 정보와 함께 제공됩니다.\n\n");

        // 클러스터별로 그룹화된 뉴스 정보
        Map<String, NewsCluster> keywordToCluster = new HashMap<>();
        for (NewsCluster cluster : clusters) {
            keywordToCluster.put(cluster.getKeyword(), cluster);
        }

        // 후보 뉴스 목록 작성
        for (int i = 0; i < candidates.size(); i++) {
            RawArticle article = candidates.get(i);

            // 해당 뉴스가 속한 클러스터 찾기
            String clusterInfo = "";
            for (NewsCluster cluster : clusters) {
                if (cluster.getArticles().contains(article)) {
                    clusterInfo = String.format("[클러스터: %s, 크기: %d개, 언론사: %d개]",
                            cluster.getKeyword(),
                            cluster.getClusterSize(),
                            cluster.getPressDiversity());
                    break;
                }
            }

            prompt.append(String.format("%d. %s [%s %d위] %s\n",
                    i + 1,
                    clusterInfo,
                    article.getPress(),
                    article.getRank(),
                    article.getTitle()));
        }

        prompt.append("\n=== 선택 규칙 ===\n\n");
        prompt.append("**절대 규칙 (반드시 준수):**\n");
        prompt.append("1. 각 클러스터에서 무조건 1개만 선택\n");
        prompt.append("2. 정확히 20개를 선택\n\n");
        prompt.append("**선택 방법:**\n");
        prompt.append("1. 클러스터를 크기 순으로 정렬 (크기가 클수록 사회적 영향력이 큼)\n");
        prompt.append("2. 큰 클러스터부터 순서대로 1개씩 선택\n");
        prompt.append("3. 20개가 될 때까지 계속 선택\n\n");
        prompt.append("**응답 형식 (번호만):**\n");
        prompt.append("1. [번호]\n");
        prompt.append("2. [번호]\n");
        prompt.append("...\n");
        prompt.append("20. [번호]\n\n");
        prompt.append("⚠️ 주의: 같은 클러스터명이 반복되면 안 됩니다. 반드시 20개를 선택하세요.");

        return prompt.toString();
    }

    /**
     * Gemini API 호출
     */
    private String callGeminiApi(String prompt) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("x-goog-api-key", geminiApiKey);

            Map<String, Object> requestBody = new HashMap<>();
            List<Map<String, Object>> contents = new ArrayList<>();
            Map<String, Object> content = new HashMap<>();
            List<Map<String, String>> parts = new ArrayList<>();
            Map<String, String> part = new HashMap<>();
            part.put("text", prompt);
            parts.add(part);
            content.put("parts", parts);
            contents.add(content);
            requestBody.put("contents", contents);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            ResponseEntity<Map> response = restTemplate.postForEntity(
                    geminiApiUrl,
                    entity,
                    Map.class
            );

            if (response.getBody() != null) {
                List<Map<String, Object>> candidates = (List<Map<String, Object>>) response.getBody().get("candidates");
                if (candidates != null && !candidates.isEmpty()) {
                    Map<String, Object> candidate = candidates.get(0);
                    Map<String, Object> contentMap = (Map<String, Object>) candidate.get("content");
                    List<Map<String, String>> partsList = (List<Map<String, String>>) contentMap.get("parts");
                    if (partsList != null && !partsList.isEmpty()) {
                        String result = partsList.get(0).get("text");
                        log.debug("Gemini 응답:\n{}", result);
                        return result;
                    }
                }
            }

            log.error("Gemini API 응답이 비어있습니다.");
            return "";

        } catch (Exception e) {
            log.error("Gemini API 호출 실패: {}", e.getMessage(), e);
            return "";
        }
    }

    /**
     * Gemini 응답 파싱
     */
    private List<RawArticle> parseGeminiResponse(String geminiResponse, List<RawArticle> candidates) {
        List<RawArticle> selectedNews = new ArrayList<>();

        // 정규식: "1. [번호]" 또는 "1. 번호" 형식
        Pattern pattern = Pattern.compile("\\d+\\.\\s*\\[?(\\d+)\\]?");
        Matcher matcher = pattern.matcher(geminiResponse);

        while (matcher.find()) {
            try {
                int index = Integer.parseInt(matcher.group(1)) - 1;  // 1-based → 0-based

                if (index >= 0 && index < candidates.size()) {
                    RawArticle article = candidates.get(index);

                    // 중복 방지
                    if (!selectedNews.contains(article)) {
                        selectedNews.add(article);
                        log.debug("선택: [{}] {}", index + 1, article.getTitle());
                    }
                }

                if (selectedNews.size() >= 20) {
                    break;
                }

            } catch (NumberFormatException e) {
                log.warn("번호 파싱 실패: {}", matcher.group(1));
            }
        }

        // 20개가 안 되면 Phase 1 결과로 채우기
        if (selectedNews.size() < 20) {
            log.warn("Gemini 선정 결과가 {}개만 있음 - 나머지는 Phase 1 결과로 채움", selectedNews.size());

            for (RawArticle candidate : candidates) {
                if (!selectedNews.contains(candidate)) {
                    selectedNews.add(candidate);
                    if (selectedNews.size() >= 20) break;
                }
            }
        }

        return selectedNews;
    }
}
