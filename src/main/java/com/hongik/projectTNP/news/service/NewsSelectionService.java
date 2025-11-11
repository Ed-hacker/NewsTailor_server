package com.hongik.projectTNP.news.service;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hongik.projectTNP.news.crawler.NaverSectionCrawler;
import com.hongik.projectTNP.news.crawler.NewsSection;
import com.hongik.projectTNP.news.domain.SummaryNewsCache;
import com.hongik.projectTNP.news.domain.SummaryNewsCacheRepository;
import com.hongik.projectTNP.news.dto.RawArticle;
import com.hongik.projectTNP.news.dto.SummaryNewsDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class NewsSelectionService {

    private final NaverSectionCrawler naverSectionCrawler;
    private final SummaryNewsCacheRepository summaryNewsCacheRepository;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${gemini.api.key}")
    private String geminiApiKey;

    @Value("${gemini.api.url}")
    private String geminiApiUrl;

    /**
     * 특정 카테고리의 뉴스 중에서 Gemini가 4개를 선별하고 요약
     */
    public List<SummaryNewsDto> selectAndSummarizeTop4(Integer sectionId) {
        // 1. 섹션별 최신 뉴스 크롤링
        NewsSection section = NewsSection.fromSectionId(sectionId);
        List<RawArticle> allNews = naverSectionCrawler.crawlSection(section);

        if (allNews.isEmpty()) {
            log.warn("섹션 {}에 대한 뉴스가 없습니다.", sectionId);
            return Collections.emptyList();
        }

        // 2. Gemini에게 뉴스 목록 전달 및 4개 선별 + 요약 요청
        String geminiResponse = requestGeminiSelection(sectionId, allNews);

        // 3. Gemini 응답 파싱
        return parseGeminiResponse(geminiResponse, allNews, sectionId);
    }

    /**
     * Gemini API 호출: 뉴스 목록 중 중요한 4개 선택 및 요약
     */
    private String requestGeminiSelection(Integer sectionId, List<RawArticle> newsList) {
        String sectionName = NewsSection.fromSectionId(sectionId).getSectionName();

        // 뉴스 목록을 텍스트로 변환 (번호, 제목, 언론사)
        StringBuilder newsListText = new StringBuilder();
        for (int i = 0; i < newsList.size(); i++) {
            RawArticle news = newsList.get(i);
            newsListText.append(String.format("%d. [%s] %s\n",
                    i + 1,
                    news.getPress(),
                    news.getTitle()
            ));
        }

        // Gemini에게 보낼 프롬프트 (키워드 빈도 기반 중요도 판단)
        String prompt = String.format(
                "다음은 '%s' 카테고리의 뉴스 목록입니다.\n\n" +
                        "%s\n" +
                        "위 뉴스 목록에서 가장 중요한 뉴스 4개를 다음 기준으로 선택하고, 각 뉴스를 3문장 이내로 요약해주세요:\n\n" +
                        "**선택 기준:**\n" +
                        "1. 여러 뉴스에서 반복되는 키워드나 주제를 찾으세요 (예: 특정 선거, 시위, 정책, 사건 등)\n" +
                        "2. 같은 키워드가 많이 등장할수록 그 주제가 중요하다고 판단하세요\n" +
                        "3. 가장 많이 반복되는 키워드를 포함한 뉴스를 1순위로, 그 다음 빈도순으로 4개를 선택하세요\n" +
                        "4. 중복 주제는 제외하고, 서로 다른 주요 이슈 4개를 선택하세요\n\n" +
                        "**응답 형식 (반드시 이 형식만 사용):**\n" +
                        "1. [번호]\n" +
                        "요약: (3문장 이내 요약)\n\n" +
                        "2. [번호]\n" +
                        "요약: (3문장 이내 요약)\n\n" +
                        "3. [번호]\n" +
                        "요약: (3문장 이내 요약)\n\n" +
                        "4. [번호]\n" +
                        "요약: (3문장 이내 요약)\n\n" +
                        "**중요:**\n" +
                        "- 번호 외에 다른 내용(제목, 언론사명 등)을 추가하지 마세요\n" +
                        "- 올바른 예시: \"1. [40]\" \n" +
                        "- 잘못된 예시: \"1. **[40] 헤럴드경제] 제목**\"",
                sectionName,
                newsListText.toString()
        );

        log.info("Gemini API 호출 - 섹션: {}, 뉴스 개수: {}", sectionName, newsList.size());

        // Exponential Backoff 재시도 로직
        int maxRetries = 5;
        int retryCount = 0;
        long delayMs = 1000; // 초기 대기 시간 1초

        while (retryCount < maxRetries) {
            try {
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                headers.set("x-goog-api-key", geminiApiKey);

                GeminiRequest requestBody = new GeminiRequest(prompt);
                HttpEntity<GeminiRequest> entity = new HttpEntity<>(requestBody, headers);

                ResponseEntity<GeminiResponse> response = restTemplate.postForEntity(
                        geminiApiUrl,
                        entity,
                        GeminiResponse.class
                );

                if (response.getBody() != null &&
                        response.getBody().getCandidates() != null &&
                        !response.getBody().getCandidates().isEmpty()) {

                    String result = response.getBody().getCandidates().get(0)
                            .getContent().getParts().get(0).getText();

                    log.info("Gemini API 성공 - 섹션: {}, 재시도 횟수: {}", sectionName, retryCount);
                    log.debug("Gemini 응답 내용:\n{}", result);
                    return result;
                } else {
                    log.error("Gemini API 응답이 비어있습니다.");
                    return "";
                }
            } catch (Exception e) {
                retryCount++;

                if (retryCount >= maxRetries) {
                    log.error("Gemini API 최종 실패 - 섹션: {}, 재시도 {}회 후 실패: {}",
                            sectionName, maxRetries, e.getMessage());
                    return "";
                }

                log.warn("Gemini API 실패 (재시도 {}/{}) - 섹션: {}, 에러: {}, {}ms 후 재시도",
                        retryCount, maxRetries, sectionName, e.getMessage(), delayMs);

                try {
                    Thread.sleep(delayMs);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    log.error("재시도 대기 중 인터럽트 발생");
                    return "";
                }

                // Exponential Backoff: 대기 시간을 2배로 증가 (1초 → 2초 → 4초 → 8초 → 16초)
                delayMs *= 2;
            }
        }

        return "";
    }

    /**
     * Gemini 응답 파싱: 선택된 뉴스 번호와 요약 추출
     */
    private List<SummaryNewsDto> parseGeminiResponse(String geminiResponse, List<RawArticle> allNews, Integer sectionId) {
        List<SummaryNewsDto> result = new ArrayList<>();

        if (geminiResponse == null || geminiResponse.isEmpty()) {
            log.error("Gemini 응답이 비어있어 파싱할 수 없습니다.");
            return result;
        }

        String sectionName = NewsSection.fromSectionId(sectionId).getSectionName();

        // 유연한 정규식: 다양한 형식 허용
        // 형식 1: "1.  [30] 매일신문] 제목\n    요약: ..."
        // 형식 2: "1. [1] 제목\n요약: ..."
        // 숫자와 대괄호 사이 공백, 들여쓰기 등 모두 허용
        Pattern pattern = Pattern.compile("(\\d+)\\.\\s*\\[(\\d+)\\].*?\\n\\s*요약:\\s*(.+?)(?=\\n\\n\\d+\\.|\\z)", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(geminiResponse);

        while (matcher.find()) {
            try {
                int originalIndex = Integer.parseInt(matcher.group(2)) - 1;  // 번호는 1부터 시작하므로 -1
                String summary = matcher.group(3).trim();

                // 다음 항목 번호나 불필요한 텍스트 제거
                summary = summary.replaceAll("\\n\\n\\d+\\..*", "").trim();

                if (originalIndex >= 0 && originalIndex < allNews.size()) {
                    RawArticle selectedNews = allNews.get(originalIndex);

                    SummaryNewsDto dto = SummaryNewsDto.builder()
                            .sectionId(sectionId)
                            .sectionName(sectionName)
                            .title(selectedNews.getTitle())
                            .url(selectedNews.getUrl())
                            .summary(summary)
                            .build();

                    result.add(dto);
                    log.debug("뉴스 선택 완료 - 번호: [{}], 제목: {}, 요약 길이: {}",
                            originalIndex + 1, selectedNews.getTitle(), summary.length());
                }
            } catch (Exception e) {
                log.error("Gemini 응답 파싱 중 오류: {}", e.getMessage(), e);
            }
        }

        // 4개가 파싱되지 않았을 경우 로그 경고
        if (result.size() != 4) {
            log.warn("Gemini가 4개가 아닌 {}개의 뉴스를 반환했습니다. 섹션: {}", result.size(), sectionName);
        }

        return result;
    }

    /**
     * 모든 카테고리의 요약 뉴스를 생성하고 DB에 캐시
     * 스케줄러에서 호출되는 메서드 (하루 3번)
     */
    @Transactional
    public void generateAndCacheAllSections() {
        log.info("===== 요약 뉴스 생성 시작 =====");
        LocalDateTime startTime = LocalDateTime.now();

        int successCount = 0;
        int failCount = 0;

        // 모든 섹션에 대해 반복
        for (NewsSection section : NewsSection.values()) {
            try {
                Integer sectionId = section.getSectionId();
                String sectionName = section.getSectionName();

                log.info("섹션 {} ({}) 요약 뉴스 생성 시작", sectionName, sectionId);

                // 1. 해당 섹션의 기존 캐시 모두 삭제 (북마크는 별도 보관)
                summaryNewsCacheRepository.deleteBySectionId(sectionId);
                log.debug("섹션 {} 기존 캐시 삭제 완료", sectionName);

                // 2. Gemini를 통해 4개 선별 및 요약
                List<SummaryNewsDto> summaries = selectAndSummarizeTop4(sectionId);

                if (summaries.isEmpty()) {
                    log.warn("섹션 {} 요약 생성 실패 - 뉴스 없음", sectionName);
                    failCount++;
                    continue;
                }

                // 3. DB에 저장 (rankOrder 1~4)
                for (int i = 0; i < summaries.size(); i++) {
                    SummaryNewsDto dto = summaries.get(i);

                    SummaryNewsCache cache = SummaryNewsCache.builder()
                            .sectionId(sectionId)
                            .sectionName(sectionName)
                            .title(dto.getTitle())
                            .url(dto.getUrl())
                            .summary(dto.getSummary())
                            .rankOrder(i + 1)  // 1부터 시작
                            .generatedAt(LocalDateTime.now())
                            .build();

                    summaryNewsCacheRepository.save(cache);
                }

                log.info("섹션 {} ({}) 요약 뉴스 {} 개 저장 완료", sectionName, sectionId, summaries.size());
                successCount++;

            } catch (Exception e) {
                log.error("섹션 {} 요약 뉴스 생성 실패: {}", section.getSectionName(), e.getMessage(), e);
                failCount++;
            }
        }

        LocalDateTime endTime = LocalDateTime.now();
        long durationSeconds = java.time.Duration.between(startTime, endTime).getSeconds();

        log.info("===== 요약 뉴스 생성 완료 =====");
        log.info("성공: {} 개 섹션, 실패: {} 개 섹션, 소요 시간: {} 초", successCount, failCount, durationSeconds);
    }

    // Gemini API 요청/응답 DTO
    private static class GeminiRequest {
        private List<Content> contents;

        public GeminiRequest() {}

        public GeminiRequest(String text) {
            Part part = new Part(text);
            Content content = new Content(Collections.singletonList(part));
            this.contents = Collections.singletonList(content);
        }

        public List<Content> getContents() {
            return contents;
        }

        public void setContents(List<Content> contents) {
            this.contents = contents;
        }
    }

    private static class Content {
        private List<Part> parts;

        public Content() {}

        public Content(List<Part> parts) {
            this.parts = parts;
        }

        public List<Part> getParts() {
            return parts;
        }

        public void setParts(List<Part> parts) {
            this.parts = parts;
        }
    }

    private static class Part {
        private String text;

        public Part() {}

        public Part(String text) {
            this.text = text;
        }

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }
    }

    private static class GeminiResponse {
        private List<Candidate> candidates;

        public List<Candidate> getCandidates() {
            return candidates;
        }

        public void setCandidates(List<Candidate> candidates) {
            this.candidates = candidates;
        }
    }

    private static class Candidate {
        private Content content;
        @JsonProperty("finishReason")
        private String finishReason;

        public Content getContent() {
            return content;
        }

        public void setContent(Content content) {
            this.content = content;
        }

        public String getFinishReason() {
            return finishReason;
        }

        public void setFinishReason(String finishReason) {
            this.finishReason = finishReason;
        }
    }
}
