package com.hongik.projectTNP.service.impl;

import com.fasterxml.jackson.annotation.JsonProperty; // JSON 직렬화/역직렬화를 위함
import com.hongik.projectTNP.agent.AgentRunner;
import com.hongik.projectTNP.agent.SummaryContext;
import com.hongik.projectTNP.domain.News;
import com.hongik.projectTNP.domain.Summary;
import com.hongik.projectTNP.repository.SummaryRepository;
import com.hongik.projectTNP.service.SummaryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate; // RestTemplate 주입
import org.springframework.context.annotation.Primary; // @Primary 어노테이션 추가

import java.util.Collections; // Collections.singletonList 사용
import java.util.List;        // List 사용

@Slf4j
@Service
// @Primary // OpenAI 서비스 비활성화, Gemini 서비스가 @Primary로 설정됨
@RequiredArgsConstructor
public class SummaryServiceImpl implements SummaryService {

    private final SummaryRepository summaryRepository;
    private final RestTemplate restTemplate; // OpenAI API 호출용
    // private final AgentRunner<SummaryContext, String> summaryAgentRunner; // AgentRunner 사용시

    @Value("${openai.api.key:YOUR_OPENAI_API_KEY}")
    private String openaiApiKey;
    @Value("${openai.api.url:https://api.openai.com/v1/chat/completions}") // Chat Completions API URL로 변경
    private String openaiApiUrl;
    @Value("${openai.api.model:gpt-3.5-turbo}") // application.yml 에 모델 설정 추가 가능
    private String openaiModel;

    @Override
    @Transactional
    public Summary summarizeNews(News news) {
        if (news == null || news.getContent() == null || news.getContent().isEmpty()) {
            log.warn("요약할 내용이 없는 뉴스입니다: News ID {}", news != null ? news.getId() : "null");
            return null; 
        }
        // 뉴스 내용이 너무 길 경우 OpenAI API 제약에 맞춰 자르기 (예: 4000 토큰 제한 고려)
        // 여기서는 간단히 문자열 길이로 제한하지만, 실제로는 토큰 기반으로 처리하는 것이 더 정확합니다.
        String contentToSummarize = news.getContent();
        int maxLength = 1500; // 예시 길이, 실제 토큰 수 고려 필요
        if (contentToSummarize.length() > maxLength) {
            contentToSummarize = contentToSummarize.substring(0, maxLength);
        }

        String summarizedText = generateSummaryWithOpenAI(contentToSummarize); 

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
         // 뉴스 내용이 너무 길 경우 OpenAI API 제약에 맞춰 자르기
        int maxLength = 1500; 
        if (textToSummarize.length() > maxLength) {
            textToSummarize = textToSummarize.substring(0, maxLength);
        }
        return generateSummaryWithOpenAI(textToSummarize);
    }

    private String generateSummaryWithOpenAI(String text) {
        log.info("OpenAI API ({}) 호출하여 텍스트 요약 시도 (모델: {})...", openaiApiUrl, openaiModel);
        if (openaiApiKey == null || openaiApiKey.isEmpty() || openaiApiKey.equals("YOUR_OPENAI_API_KEY")) {
            log.warn("OpenAI API 키가 설정되지 않았습니다. application.yml 파일을 확인해주세요. 임시 요약을 반환합니다.");
            return "[임시 요약] " + text.substring(0, Math.min(text.length(), 50)) + "...";
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(openaiApiKey);
        headers.setContentType(MediaType.APPLICATION_JSON);

        // 한국어 요약을 위한 프롬프트 수정
        String prompt = "다음 뉴스 기사를 한국어로 3문장 이내로 요약해줘: \n" + text;
        
        MessageDto message = new MessageDto("user", prompt);
        OpenAiChatRequest requestBody = new OpenAiChatRequest(openaiModel, Collections.singletonList(message));

        HttpEntity<OpenAiChatRequest> entity = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<OpenAiChatResponse> response = restTemplate.postForEntity(openaiApiUrl, entity, OpenAiChatResponse.class);
            if (response.getBody() != null && response.getBody().getChoices() != null && !response.getBody().getChoices().isEmpty()) {
                String summary = response.getBody().getChoices().get(0).getMessage().getContent();
                log.info("OpenAI API로부터 요약 수신 완료.");
                return summary.trim();
            } else {
                log.warn("OpenAI API로부터 유효한 요약 응답을 받지 못했습니다. 응답: {}", response.getBody());
                return "[요약 실패]";
            }
        } catch (Exception e) {
            log.error("OpenAI API 호출 중 오류 발생: {}", e.getMessage(), e);
            return "[요약 중 오류 발생]";
        }
    }

    // OpenAI Chat Completions API 요청/응답 DTO
    private static class OpenAiChatRequest {
        private String model;
        private List<MessageDto> messages;
        // 추가 파라미터 (temperature, max_tokens 등) 필요시 추가

        public OpenAiChatRequest(String model, List<MessageDto> messages) {
            this.model = model;
            this.messages = messages;
        }
        public String getModel() { return model; }
        public List<MessageDto> getMessages() { return messages; }
    }

    private static class MessageDto {
        private String role;
        private String content;

        public MessageDto(String role, String content) {
            this.role = role;
            this.content = content;
        }
        public String getRole() { return role; }
        public String getContent() { return content; }
    }

    private static class OpenAiChatResponse {
        private List<Choice> choices;
        // 기타 필드 (id, object, created, model, usage 등) 필요시 추가

        public List<Choice> getChoices() { return choices; }
        public void setChoices(List<Choice> choices) { this.choices = choices; }
    }

    private static class Choice {
        private MessageDto message;
        @JsonProperty("finish_reason")
        private String finishReason;
        private int index;

        public MessageDto getMessage() { return message; }
        public void setMessage(MessageDto message) { this.message = message; }
        public String getFinishReason() { return finishReason; }
        public void setFinishReason(String finishReason) { this.finishReason = finishReason; }
        public int getIndex() { return index; }
        public void setIndex(int index) { this.index = index; }
    }
} 