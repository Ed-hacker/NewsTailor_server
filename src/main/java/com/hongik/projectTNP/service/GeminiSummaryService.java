package com.hongik.projectTNP.service;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.hongik.projectTNP.domain.News;
import com.hongik.projectTNP.domain.Summary;
import com.hongik.projectTNP.repository.SummaryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;

@Slf4j
@Service
@Primary
@RequiredArgsConstructor
public class GeminiSummaryService implements SummaryService {

    private final SummaryRepository summaryRepository;
    private final RestTemplate restTemplate;

    @Value("${gemini.api.key:YOUR_GEMINI_API_KEY}")
    private String geminiApiKey;

    @Value("${gemini.api.url:https://generativelanguage.googleapis.com/v1/models/gemini-1.5-flash:generateContent}")
    private String geminiApiUrl;

    @Override
    @Transactional
    public Summary summarizeNews(News news) {
        if (news == null || news.getContent() == null || news.getContent().isEmpty()) {
            log.warn("요약할 내용이 없는 뉴스입니다: News ID {}", news != null ? news.getId() : "null");
            return null;
        }

        String contentToSummarize = news.getContent();
        int maxLength = 2000; // Gemini는 더 긴 입력을 처리할 수 있음
        if (contentToSummarize.length() > maxLength) {
            contentToSummarize = contentToSummarize.substring(0, maxLength);
        }

        String summarizedText = generateSummaryWithGemini(contentToSummarize);

        Summary summary = Summary.builder()
                .news(news)
                .summary_text(summarizedText)
                .build();
        return summaryRepository.save(summary);
    }

    @Override
    public String generateSummary(String textToSummarize) {
        if (textToSummarize == null || textToSummarize.isEmpty()) {
            return "";
        }

        int maxLength = 2000;
        if (textToSummarize.length() > maxLength) {
            textToSummarize = textToSummarize.substring(0, maxLength);
        }
        return generateSummaryWithGemini(textToSummarize);
    }

    private String generateSummaryWithGemini(String text) {
        log.info("Gemini API ({}) 호출하여 텍스트 요약 시도...", geminiApiUrl);

        if (geminiApiKey == null || geminiApiKey.isEmpty() || geminiApiKey.equals("YOUR_GEMINI_API_KEY")) {
            log.warn("Gemini API 키가 설정되지 않았습니다. application.properties 파일을 확인해주세요. 임시 요약을 반환합니다.");
            return "[임시 요약] " + text.substring(0, Math.min(text.length(), 50)) + "...";
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        String prompt = "다음 뉴스 기사를 한국어로 3문장 이내로 요약해주세요:\n\n" + text;

        GeminiRequest requestBody = new GeminiRequest(prompt);
        HttpEntity<GeminiRequest> entity = new HttpEntity<>(requestBody, headers);

        try {
            String urlWithKey = geminiApiUrl + "?key=" + geminiApiKey;
            ResponseEntity<GeminiResponse> response = restTemplate.postForEntity(urlWithKey, entity, GeminiResponse.class);

            if (response.getBody() != null &&
                response.getBody().getCandidates() != null &&
                !response.getBody().getCandidates().isEmpty()) {

                String summary = response.getBody().getCandidates().get(0)
                    .getContent().getParts().get(0).getText();
                log.info("Gemini API로부터 요약 수신 완료.");
                return summary.trim();
            } else {
                log.warn("Gemini API로부터 유효한 요약 응답을 받지 못했습니다. 응답: {}", response.getBody());
                return "[요약 실패]";
            }
        } catch (Exception e) {
            log.error("Gemini API 호출 중 오류 발생: {}", e.getMessage(), e);
            return "[요약 중 오류 발생: " + e.getMessage() + "]";
        }
    }

    // Gemini API 요청/응답 DTO 클래스들
    private static class GeminiRequest {
        private List<Content> contents;

        public GeminiRequest(String text) {
            Part part = new Part(text);
            Content content = new Content(Collections.singletonList(part));
            this.contents = Collections.singletonList(content);
        }

        public List<Content> getContents() { return contents; }
    }

    private static class Content {
        private List<Part> parts;

        public Content(List<Part> parts) {
            this.parts = parts;
        }

        public List<Part> getParts() { return parts; }
    }

    private static class Part {
        private String text;

        public Part(String text) {
            this.text = text;
        }

        public String getText() { return text; }
    }

    private static class GeminiResponse {
        private List<Candidate> candidates;

        public List<Candidate> getCandidates() { return candidates; }
        public void setCandidates(List<Candidate> candidates) { this.candidates = candidates; }
    }

    private static class Candidate {
        private Content content;
        @JsonProperty("finishReason")
        private String finishReason;

        public Content getContent() { return content; }
        public void setContent(Content content) { this.content = content; }
        public String getFinishReason() { return finishReason; }
        public void setFinishReason(String finishReason) { this.finishReason = finishReason; }
    }
}