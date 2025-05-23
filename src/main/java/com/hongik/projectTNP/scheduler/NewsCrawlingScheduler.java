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
            // 검색어를 추가하고 카테고리 제한을 제거
            String url = "https://newsapi.org/v2/everything?apiKey=" + apiKey + "&q=technology OR AI OR software&language=en&sortBy=publishedAt&pageSize=10";
            logger.info("API 요청 URL (키 제외): {}", url.replace(apiKey, "API_KEY"));
            
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);
            logger.info("API 응답 상태: {}", response != null ? response.get("status") : "응답 없음");
            logger.info("전체 API 응답: {}", response);
            
            if (response != null && "ok".equals(response.get("status"))) {
                List<Map<String, Object>> articles = (List<Map<String, Object>>) response.get("articles");
                logger.info("API에서 받은 뉴스 개수: {}", articles != null ? articles.size() : 0);
                
                if (articles == null || articles.isEmpty()) {
                    logger.info("API에서 받은 뉴스가 없습니다.");
                    return;
                }
                
                List<News> savedNews = new ArrayList<>();
                
                for (Map<String, Object> article : articles) {
                    String title = (String) article.get("title");
                    String articleUrl = (String) article.get("url");
                    String content = (String) article.get("content");
                    
                    logger.info("처리 중인 뉴스: title={}, url={}", title, articleUrl);
                    
                    // 이미 존재하는 뉴스인지 확인
                    if (newsService.existsByUrl(articleUrl)) {
                        logger.info("이미 존재하는 뉴스입니다: {}", articleUrl);
                        continue;
                    }
                    
                    News news = News.builder()
                            .title(title)
                            .url(articleUrl)
                            .content(content)
                            .publishedAt(LocalDateTime.now())
                            .build();
                    
                    savedNews.add(newsService.save(news));
                    logger.info("새로운 뉴스 저장됨: {}", title);
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