package com.hongik.projectTNP.scheduler;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties; // DTO용 어노테이션
import com.hongik.projectTNP.domain.News;
import com.hongik.projectTNP.repository.NewsRepository;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder; // URI 빌더 사용

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZonedDateTime; // API 응답의 시간대 처리
import java.time.format.DateTimeFormatter; // 날짜 파싱
import java.util.List;          // DTO용

@Component
@RequiredArgsConstructor
public class NewsCrawlingScheduler {

    private static final Logger log = LoggerFactory.getLogger(NewsCrawlingScheduler.class); // 로거 추가

    private final NewsRepository newsRepository;
    private final RestTemplate restTemplate;

    @Value("${news.api.url:https://newsapi.org/v2/everything}")
    private String newsApiUrl;
    @Value("${news.api.key:YOUR_NEWS_API_KEY}") // 실제 NewsAPI.org 키 필요
    private String newsApiKey;
    @Value("${news.api.query:technology}") // 검색 쿼리
    private String newsApiQuery;
    @Value("${news.api.language:en}")    // 언어
    private String newsApiLanguage;

    @Scheduled(cron = "0 0 9,13,19 * * *") // 하루 3회 실행
    @Transactional
    public void crawlNews() {
        log.info("뉴스 크롤링 스케줄러 시작 - API: {}, Query: {}", newsApiUrl, newsApiQuery);

        if (newsApiKey == null || newsApiKey.isEmpty() || newsApiKey.equals("YOUR_NEWS_API_KEY")) {
            log.warn("NewsAPI 키가 설정되지 않았습니다. application.yml/properties 파일을 확인해주세요. 더미 데이터를 사용합니다.");
            processArticle(getDummyArticle()); // 키가 없으면 더미 데이터 사용
            log.info("뉴스 크롤링 스케줄러 종료 (더미 데이터 사용)");
            return;
        }

        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromHttpUrl(newsApiUrl)
                .queryParam("q", newsApiQuery)
                .queryParam("language", newsApiLanguage)
                .queryParam("sortBy", "publishedAt") // 최신순 정렬
                .queryParam("pageSize", 20)       // 가져올 기사 수 (최대 100)
                .queryParam("apiKey", newsApiKey);
        try {
            NewsApiResponse response = restTemplate.getForObject(uriBuilder.toUriString(), NewsApiResponse.class);

            if (response != null && response.getArticles() != null) {
                log.info("NewsAPI로부터 {}개의 기사 수신", response.getArticles().size());
                for (ArticleDto articleDto : response.getArticles()) {
                    processArticle(articleDto);
                }
            } else {
                log.warn("NewsAPI로부터 응답이 없거나 기사가 없습니다.");
            }
        } catch (HttpClientErrorException e) {
            log.error("NewsAPI 호출 중 클라이언트 오류 발생 ({} {}): {}", e.getStatusCode(), e.getStatusText(), e.getResponseBodyAsString());
        } catch (Exception e) {
            log.error("NewsAPI 호출 또는 처리 중 예외 발생: {}", e.getMessage(), e);
        }
        log.info("뉴스 크롤링 스케줄러 종료");
    }

    private void processArticle(ArticleDto articleDto) {
        if (articleDto == null || articleDto.getUrl() == null || articleDto.getUrl().isEmpty() || articleDto.getTitle() == null || articleDto.getTitle().isEmpty()) {
            log.warn("유효하지 않은 기사 데이터 수신: {}", articleDto);
            return;
        }
        // 제목이나 URL이 너무 짧거나 placeholder 같은 경우 필터링 (선택적)
        if (articleDto.getTitle().equalsIgnoreCase("[Removed]") || articleDto.getUrl().contains("removed.com")){
             log.info("제거된 기사로 판단되어 스킵: {}", articleDto.getTitle());
             return;
        }

        if (!newsRepository.existsByUrl(articleDto.getUrl())) {
            String fullContent = "";
            try {
                log.info("URL 크롤링 시도: {}", articleDto.getUrl());
                Document doc = Jsoup.connect(articleDto.getUrl()).timeout(10000).get();
                // 아래 selector는 예시이며, 실제 기사 구조에 맞게 수정 필요
                // fullContent = doc.selectFirst("article, .article-body, #article_content")?.text() ?? doc.body().text();
                fullContent = doc.body().text(); // 임시로 body 전체 텍스트 사용
                if (fullContent.length() > 2000) { 
                    fullContent = fullContent.substring(0, 1997) + "...";
                }
                if (fullContent.trim().isEmpty()){
                    log.warn("크롤링된 내용이 비어있습니다. URL: {}", articleDto.getUrl());
                    // 비어있는 content로 저장하지 않으려면 여기서 return 또는 예외처리
                }

                News news = News.builder()
                        .title(articleDto.getTitle())
                        .url(articleDto.getUrl())
                        .content(fullContent)
                        .category(extractCategory(articleDto, newsApiQuery))
                        .publishedAt(parseDateTime(articleDto.getPublishedAt())) // API 응답의 날짜 형식에 맞춰 파싱
                        .build();
                newsRepository.save(news);
                log.info("새로운 뉴스 저장: {}", news.getTitle());
            } catch (IOException e) {
                log.error("Jsoup 크롤링 중 IOException 발생 - URL: {}: {}", articleDto.getUrl(), e.getMessage());
            } catch (Exception e) {
                log.error("기사 처리 중 기타 오류 발생 - URL: {}: {}", articleDto.getUrl(), e.getMessage(), e);
            }
        } else {
            log.info("이미 존재하는 뉴스입니다: {}", articleDto.getUrl());
        }
    }
    
    private String extractCategory(ArticleDto articleDto, String newsApiQuery) {
        String category = newsApiQuery; // 기본값으로 전체 쿼리 사용
        if (newsApiQuery != null && !newsApiQuery.isEmpty()) {
            if (newsApiQuery.contains(",")) {
                category = newsApiQuery.split(",")[0].trim(); // 첫 번째 쿼리를 카테고리로 사용
            }
        }
        // API에서 제공하는 source name을 카테고리로 사용하고 싶다면 아래 로직 활용
        // if (articleDto.getSource() != null && articleDto.getSource().getName() != null) {
        //     category = articleDto.getSource().getName();
        // }
        return category;
    }

    private LocalDateTime parseDateTime(String dateTimeString) {
        if (dateTimeString == null) return LocalDateTime.now();
        try {
            // NewsAPI는 보통 ISO 8601 형식이지만, ZonedDateTime으로 받아 처리 후 LocalDateTime으로 변환
            return ZonedDateTime.parse(dateTimeString, DateTimeFormatter.ISO_DATE_TIME).toLocalDateTime();
        } catch (Exception e) {
            log.warn("날짜 파싱 실패 '{}': {}. 현재 시간을 사용합니다.", dateTimeString, e.getMessage());
            return LocalDateTime.now(); 
        }
    }

    // NewsAPI.org 응답 DTO (내부 클래스 또는 별도 파일)
    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class NewsApiResponse {
        private String status;
        private Integer totalResults;
        private List<ArticleDto> articles;
        public List<ArticleDto> getArticles() { return articles; }
        public void setArticles(List<ArticleDto> articles) { this.articles = articles; }
        // status, totalResults getter/setter 생략 가능 (사용하지 않는다면)
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class ArticleDto { // 기존 DummyArticle 대신 사용
        private SourceDto source;
        private String author;
        private String title;
        private String description;
        private String url;
        private String urlToImage;
        private String publishedAt; // 예: "2023-10-26T08:30:00Z"
        private String content; // API에서 제공하는 짧은 content (원문과 다름)

        public SourceDto getSource() { return source; }
        public void setSource(SourceDto source) { this.source = source; }
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public String getUrl() { return url; }
        public void setUrl(String url) { this.url = url; }
        public String getPublishedAt() { return publishedAt; }
        public void setPublishedAt(String publishedAt) { this.publishedAt = publishedAt; }
        // 나머지 getter/setter 생략 가능 (사용하지 않는다면)
        @Override public String toString() { return "ArticleDto{title='" + title + "\', url='" + url + "\'}";}
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class SourceDto {
        private String id;
        private String name;
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        // id getter/setter 생략 가능
    }

    // 임시 더미 데이터 생성 메소드 (API 키 없을 때 사용)
    private ArticleDto getDummyArticle() {
        ArticleDto dummy = new ArticleDto();
        dummy.setTitle("더미 뉴스 제목 " + System.currentTimeMillis());
        // 실제 네이버 뉴스 URL은 Jsoup 크롤링 시 User-Agent 등 추가 헤더가 필요할 수 있어 막힐 가능성 있음
        // 테스트용으로는 접근 가능한 간단한 HTML 페이지 URL 권장
        dummy.setUrl("https://www.example.com/news/" + System.currentTimeMillis()); 
        dummy.setPublishedAt(ZonedDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME));
        SourceDto source = new SourceDto();
        source.setName("정치"); // 더미 카테고리
        dummy.setSource(source);
        return dummy;
    }
} 