package com.hongik.projectTNP.news.crawler;

import com.hongik.projectTNP.news.dto.RawArticle;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 네이버 뉴스 섹션별 최신 뉴스 크롤러
 * URL: https://news.naver.com/section/{sectionId}
 */
@Slf4j
@Component
public class NaverSectionCrawler {

    private static final String BASE_URL = "https://news.naver.com/section/";
    private static final String USER_AGENT = "Mozilla/5.0 ClaudeCrawler/1.0";
    private static final int TIMEOUT = 7000;
    private static final int MAX_BODY_SIZE = 2 * 1024 * 1024; // 2MB
    private static final int MAX_ARTICLES = 100; // 최대 수집 기사 수

    /**
     * 특정 섹션의 최신 뉴스를 크롤링
     * @param section 뉴스 섹션
     * @return 크롤링된 기사 목록
     */
    public List<RawArticle> crawlSection(NewsSection section) {
        List<RawArticle> articles = new ArrayList<>();

        try {
            String url = BASE_URL + section.getSectionId();
            log.info("섹션 크롤링 시작 - Section: {}, URL: {}", section.getSectionName(), url);

            Document doc = Jsoup.connect(url)
                    .userAgent(USER_AGENT)
                    .timeout(TIMEOUT)
                    .maxBodySize(MAX_BODY_SIZE)
                    .get();

            // 뉴스 목록 선택자 (네이버 뉴스 섹션 페이지 구조에 맞춤)
            // 실제 선택자는 페이지 구조에 따라 조정 필요
            Elements articleElements = doc.select("ul.sa_list > li");

            if (articleElements.isEmpty()) {
                // 대체 선택자 시도
                articleElements = doc.select("div.sa_text");
            }

            int rank = 1;
            for (Element articleElement : articleElements) {
                if (rank > MAX_ARTICLES) break;

                RawArticle article = extractArticleFromElement(articleElement, section.getSectionId(), rank);
                if (article != null) {
                    articles.add(article);
                    rank++;
                }
            }

            log.info("섹션 크롤링 완료 - Section: {}, 수집된 기사 수: {}", section.getSectionName(), articles.size());

        } catch (IOException e) {
            log.error("섹션 크롤링 실패 - Section: {}, Error: {}", section.getSectionName(), e.getMessage());
        }

        return articles;
    }

    /**
     * 뉴스 요소에서 기사 정보 추출
     */
    private RawArticle extractArticleFromElement(Element element, int sectionId, int rank) {
        try {
            // 제목과 링크 추출
            Element linkElement = element.selectFirst("a.sa_text_title");

            if (linkElement == null) {
                // 대체 선택자 시도
                linkElement = element.selectFirst("a[href*=article]");
            }

            if (linkElement == null) {
                return null;
            }

            String title = linkElement.text().trim();
            String url = linkElement.attr("abs:href"); // 절대 URL로 변환

            if (title.isEmpty() || url.isEmpty()) {
                return null;
            }

            // 언론사 추출
            String press = "Unknown";
            Element pressElement = element.selectFirst("div.sa_text_press");
            if (pressElement == null) {
                pressElement = element.selectFirst("em.sa_text_press");
            }
            if (pressElement != null) {
                press = pressElement.text().trim();
            }

            return RawArticle.builder()
                    .sectionId(sectionId)
                    .press(press)
                    .rank(rank)
                    .title(title)
                    .url(url)
                    .build();

        } catch (Exception e) {
            log.warn("기사 추출 실패 - Element: {}, Error: {}", element.text(), e.getMessage());
            return null;
        }
    }
}
