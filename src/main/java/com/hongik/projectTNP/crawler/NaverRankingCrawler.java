package com.hongik.projectTNP.crawler;

import com.hongik.projectTNP.dto.news.RawArticle;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class NaverRankingCrawler {
    
    private static final String BASE_URL = "https://news.naver.com/main/ranking/popularDay.naver";
    private static final String USER_AGENT = "Mozilla/5.0 ClaudeCrawler/1.0";
    private static final int TIMEOUT = 7000;
    private static final int MAX_BODY_SIZE = 2 * 1024 * 1024; // 2MB
    
    public List<RawArticle> crawlRanking(NewsSection section, LocalDate date) {
        List<RawArticle> articles = new ArrayList<>();
        
        try {
            String url = buildUrl(section.getSectionId(), date);
            log.info("크롤링 시작 - Section: {}, Date: {}, URL: {}", section.getSectionName(), date, url);
            
            Document doc = Jsoup.connect(url)
                    .userAgent(USER_AGENT)
                    .timeout(TIMEOUT)
                    .maxBodySize(MAX_BODY_SIZE)
                    .get();
            
            Elements rankingBoxes = doc.select("div.rankingnews_box");
            
            for (Element box : rankingBoxes) {
                String press = extractPress(box);
                if (press == null) continue;
                
                Elements articleList = box.select("ul.rankingnews_list > li");
                
                for (Element articleItem : articleList) {
                    RawArticle article = extractArticleFromElement(articleItem, section.getSectionId(), press);
                    if (article != null) {
                        articles.add(article);
                    }
                }
            }
            
            log.info("크롤링 완료 - Section: {}, 수집된 기사 수: {}", section.getSectionName(), articles.size());
            
        } catch (IOException e) {
            log.error("크롤링 실패 - Section: {}, Date: {}, Error: {}", section.getSectionName(), date, e.getMessage());
        }
        
        return articles;
    }
    
    private String buildUrl(int sectionId, LocalDate date) {
        String dateStr = date.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        return BASE_URL + "?rankingType=popular_day&sectionId=" + sectionId + "&date=" + dateStr;
    }
    
    private String extractPress(Element box) {
        Element pressElement = box.selectFirst("strong.rankingnews_name");
        return pressElement != null ? pressElement.text().trim() : null;
    }
    
    private RawArticle extractArticleFromElement(Element articleItem, int sectionId, String press) {
        try {
            Element rankElement = articleItem.selectFirst("em.list_ranking_num");
            Element linkElement = articleItem.selectFirst("a");
            
            if (rankElement == null || linkElement == null) {
                return null;
            }
            
            String rankText = rankElement.text().trim();
            int rank = Integer.parseInt(rankText);
            
            String title = linkElement.text().trim();
            String url = linkElement.attr("href");
            
            if (title.isEmpty() || url.isEmpty()) {
                return null;
            }
            
            // 상대 URL을 절대 URL로 변환
            if (url.startsWith("/")) {
                url = "https://news.naver.com" + url;
            }
            
            return RawArticle.builder()
                    .sectionId(sectionId)
                    .press(press)
                    .rank(rank)
                    .title(title)
                    .url(url)
                    .build();
                    
        } catch (NumberFormatException e) {
            log.warn("순위 파싱 실패 - Element: {}", articleItem.text());
            return null;
        } catch (Exception e) {
            log.warn("기사 추출 실패 - Element: {}, Error: {}", articleItem.text(), e.getMessage());
            return null;
        }
    }
}