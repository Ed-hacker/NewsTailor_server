package com.hongik.projectTNP.service;

import com.hongik.projectTNP.domain.News;
import com.hongik.projectTNP.domain.Summary;
import com.hongik.projectTNP.repository.SummaryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class GptSummaryService implements SummaryService {

    private final SummaryRepository summaryRepository;
    private final RestTemplate restTemplate;
    
    @Value("${openai.api.key}")
    private String apiKey;
    
    @Value("${openai.api.url:https://api.openai.com/v1/chat/completions}")
    private String apiUrl;
    
    @Value("${openai.model:gpt-3.5-turbo}")
    private String model;

    @Autowired
    public GptSummaryService(SummaryRepository summaryRepository, RestTemplate restTemplate) {
        this.summaryRepository = summaryRepository;
        this.restTemplate = restTemplate;
    }

    @Override
    public Summary summarizeNews(News news) {
        String summaryText = generateSummary(news.getContent());
        return Summary.builder()
                .news(news)
                .summary_text(summaryText)
                .build();
    }

    @Override
    public String generateSummary(String textToSummarize) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + apiKey);

        Map<String, Object> messageContent = new HashMap<>();
        messageContent.put("role", "user");
        messageContent.put("content", "다음 뉴스 내용을 3-4문장으로 요약해주세요:\n\n" + textToSummarize);

        Map<String, Object> message = new HashMap<>();
        message.put("messages", List.of(messageContent));
        message.put("model", model);
        message.put("max_tokens", 300);
        message.put("temperature", 0.3);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(message, headers);
        
        try {
            Map<String, Object> response = restTemplate.postForObject(apiUrl, request, Map.class);
            
            if (response != null && response.containsKey("choices")) {
                List<Map<String, Object>> choices = (List<Map<String, Object>>) response.get("choices");
                if (!choices.isEmpty()) {
                    Map<String, Object> choice = choices.get(0);
                    Map<String, Object> responseMessage = (Map<String, Object>) choice.get("message");
                    return (String) responseMessage.get("content");
                }
            }
            return "요약을 생성하는 중 오류가 발생했습니다.";
        } catch (Exception e) {
            return "API 호출 중 오류가 발생했습니다: " + e.getMessage();
        }
    }

    public Summary findByNewsId(Long newsId) {
        return summaryRepository.findByNewsId(newsId).orElse(null);
    }

    public Summary save(Summary summary) {
        return summaryRepository.save(summary);
    }
} 