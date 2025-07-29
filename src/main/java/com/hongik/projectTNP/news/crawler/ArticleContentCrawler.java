package com.hongik.projectTNP.news.crawler;

import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
public class ArticleContentCrawler {
    
    private static final String USER_AGENT = "Mozilla/5.0 ClaudeCrawler/1.0";
    private static final int TIMEOUT = 7000;
    private static final int MAX_BODY_SIZE = 2 * 1024 * 1024; // 2MB
    private static final long CRAWL_DELAY = 1000; // 1초 지연
    
    public String extractArticleContent(String url) {
        return extractArticleContent(url, false);
    }
    
    public String extractArticleContent(String url, boolean isRetry) {
        try {
            if (!isRetry) {
                Thread.sleep(CRAWL_DELAY);
            }
            
            log.debug("기사 본문 크롤링 시작 - URL: {}", url);
            
            Document doc = Jsoup.connect(url)
                    .userAgent(USER_AGENT)
                    .timeout(TIMEOUT)
                    .maxBodySize(MAX_BODY_SIZE)
                    .get();
            
            // 우선순위에 따라 본문 선택자 시도
            String content = extractContentBySelector(doc, "#newsct_article");
            if (content != null && !content.trim().isEmpty()) {
                log.debug("본문 추출 성공 (#newsct_article) - URL: {}, 길이: {}", url, content.length());
                return content.trim();
            }
            
            content = extractContentBySelector(doc, "#dic_area");
            if (content != null && !content.trim().isEmpty()) {
                log.debug("본문 추출 성공 (#dic_area) - URL: {}, 길이: {}", url, content.length());
                return content.trim();
            }
            
            log.warn("본문을 찾을 수 없음 - URL: {}", url);
            return "";
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("본문 크롤링 중단됨 - URL: {}", url);
            return "";
        } catch (IOException e) {
            log.warn("본문 크롤링 실패 - URL: {}, Error: {}", url, e.getMessage());
            
            // 재시도 로직
            if (!isRetry) {
                log.info("본문 크롤링 재시도 - URL: {}", url);
                return extractArticleContent(url, true);
            }
            
            return "";
        } catch (Exception e) {
            log.error("본문 크롤링 예외 발생 - URL: {}, Error: {}", url, e.getMessage());
            return "";
        }
    }
    
    private String extractContentBySelector(Document doc, String selector) {
        Element contentElement = doc.selectFirst(selector);
        if (contentElement == null) {
            return null;
        }
        
        // HTML 태그 제거하고 텍스트만 추출
        String text = contentElement.text();
        
        // 빈 문자열이거나 너무 짧은 경우 null 반환
        if (text == null || text.trim().length() < 50) {
            return null;
        }
        
        return text;
    }
}