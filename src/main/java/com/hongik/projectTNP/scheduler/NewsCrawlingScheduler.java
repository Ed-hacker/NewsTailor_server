package com.hongik.projectTNP.scheduler;

import com.hongik.projectTNP.domain.News;
import com.hongik.projectTNP.service.NewsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 뉴스 기사를 주기적으로 크롤링하는 스케줄러
 */
@Component
public class NewsCrawlingScheduler {

    private static final Logger logger = LoggerFactory.getLogger(NewsCrawlingScheduler.class);
    
    private final NewsService newsService;
    private final RestTemplate restTemplate;
    
    @Value("${news.api.key}")
    private String apiKey;
    
    @Value("${news.api.url}")
    private String apiUrl;

    @Autowired
    public NewsCrawlingScheduler(NewsService newsService) {
        this.newsService = newsService;
        this.restTemplate = new RestTemplate();
    }

    /**
     * 30분마다 뉴스를 크롤링합니다.
     */
    @Scheduled(fixedRate = 1800000) // 30분 = 1,800,000ms
    public void crawlNews() {
        logger.info("뉴스 크롤링 작업 시작: {}", LocalDateTime.now());
        
        try {
            String url = apiUrl + "?apiKey=" + apiKey + "&country=kr&category=technology";
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);
            
            if (response != null && "ok".equals(response.get("status"))) {
                List<Map<String, Object>> articles = (List<Map<String, Object>>) response.get("articles");
                List<News> savedNews = new ArrayList<>();
                
                for (Map<String, Object> article : articles) {
                    String articleUrl = (String) article.get("url");
                    
                    // 이미 존재하는 뉴스인지 확인
                    if (newsService.existsByUrl(articleUrl)) {
                        continue;
                    }
                    
                    News news = News.builder()
                            .title((String) article.get("title"))
                            .url(articleUrl)
                            .content((String) article.get("content"))
                            .publishedAt(LocalDateTime.now()) // 실제로는 API에서 받은 날짜를 파싱해야 함
                            .build();
                    
                    savedNews.add(newsService.save(news));
                }
                
                logger.info("새로운 뉴스 {} 건이 크롤링되었습니다.", savedNews.size());
            } else {
                logger.error("뉴스 API 응답 오류: {}", response);
            }
        } catch (Exception e) {
            logger.error("뉴스 크롤링 중 오류가 발생했습니다: {}", e.getMessage(), e);
        }
    }
} 