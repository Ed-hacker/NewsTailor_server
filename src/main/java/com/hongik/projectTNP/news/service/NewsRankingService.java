package com.hongik.projectTNP.news.service;

import com.hongik.projectTNP.news.crawler.ArticleContentCrawler;
import com.hongik.projectTNP.news.crawler.NaverRankingCrawler;
import com.hongik.projectTNP.news.crawler.NewsSection;
import com.hongik.projectTNP.news.domain.NewsRanking;
import com.hongik.projectTNP.news.domain.NewsRankingRepository;
import com.hongik.projectTNP.news.dto.NewsRankingResponse;
import com.hongik.projectTNP.news.dto.RawArticle;
import com.hongik.projectTNP.service.SummaryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class NewsRankingService {

    private final NewsRankingRepository newsRankingRepository;
    private final NaverRankingCrawler naverRankingCrawler;
    private final ArticleContentCrawler articleContentCrawler;
    private final SummaryService summaryService;
    private final NewsRankingSelectionService newsRankingSelectionService;

    @Transactional
    public void crawlAndSaveAllSections() {
        LocalDate today = LocalDate.now();

        try {
            // 1. 기존 랭킹 뉴스 모두 삭제
            log.info("기존 랭킹 뉴스 삭제 시작");
            newsRankingRepository.deleteAll();
            newsRankingRepository.flush(); // 삭제를 DB에 즉시 반영
            log.info("기존 랭킹 뉴스 삭제 완료");

            // 2. 크롤링
            List<RawArticle> rawArticles = naverRankingCrawler.crawlRanking(NewsSection.POLITICS, today);
            log.info("크롤링 완료 - 총 {}개 뉴스", rawArticles.size());

            // 3. 하이브리드 알고리즘으로 상위 20개 선정
            List<RawArticle> top20Articles = newsRankingSelectionService.selectTop20News(rawArticles);
            log.info("하이브리드 알고리즘 선정 완료 - {}개 뉴스", top20Articles.size());

            // 4. 선정된 20개 뉴스 저장
            int savedCount = 0;
            for (RawArticle rawArticle : top20Articles) {
                try {
                    // 본문 크롤링
                    String body = articleContentCrawler.extractArticleContent(rawArticle.getUrl());

                    // Gemini로 요약 생성
                    String summary = null;
                    try {
                        summary = summaryService.generateSummary(body);
                        log.debug("요약 생성 완료 - Title: {}", rawArticle.getTitle());
                    } catch (Exception e) {
                        log.error("요약 생성 실패 - Title: {}, Error: {}", rawArticle.getTitle(), e.getMessage());
                    }

                    // 엔티티 생성 및 저장
                    NewsRanking newsRanking = NewsRanking.builder()
                            .sectionId(rawArticle.getSectionId())
                            .press(rawArticle.getPress())
                            .rank(rawArticle.getRank())
                            .title(rawArticle.getTitle())
                            .url(rawArticle.getUrl())
                            .summary(summary)
                            .build();

                    newsRankingRepository.save(newsRanking);
                    savedCount++;

                    log.debug("기사 저장 완료 - Title: {}", rawArticle.getTitle());

                } catch (Exception e) {
                    log.error("기사 저장 실패 - URL: {}, Error: {}", rawArticle.getUrl(), e.getMessage());
                    // 예외 발생 시에도 계속 진행 (일부 실패해도 나머지는 저장)
                }
            }

            log.info("랭킹 뉴스 저장 완료 - 저장된 기사 수: {}/{}", savedCount, top20Articles.size());

        } catch (Exception e) {
            log.error("랭킹 뉴스 크롤링 실패: {}", e.getMessage(), e);
            throw e; // 트랜잭션 롤백을 위해 예외 재발생
        }
    }

    @Transactional
    public void deleteAllRankingNews() {
        log.info("기존 랭킹 뉴스 삭제 시작");
        newsRankingRepository.deleteAll();
        log.info("기존 랭킹 뉴스 삭제 완료");
    }

    @Transactional
    public void deleteOldData() {
        LocalDateTime twoDaysAgo = LocalDateTime.now().minusDays(2);

        try {
            newsRankingRepository.deleteByCollectedAtBefore(twoDaysAgo);
            log.info("2일 이전 데이터 삭제 완료 - 기준 시간: {}", twoDaysAgo);
        } catch (Exception e) {
            log.error("오래된 데이터 삭제 실패: {}", e.getMessage(), e);
        }
    }
    
    @Transactional(readOnly = true)
    public List<NewsRankingResponse> getRankingsBySection(Integer sectionId, LocalDate date) {
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(LocalTime.MAX);
        
        List<NewsRanking> rankings = newsRankingRepository
                .findBySectionIdAndCollectedAtBetweenOrderByRankAsc(sectionId, startOfDay, endOfDay);
        
        return rankings.stream()
                .map(NewsRankingResponse::from)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public List<NewsRankingResponse> getTop1Rankings(int limit) {
        LocalDateTime since = LocalDateTime.now().minusHours(24);
        
        List<NewsRanking> rankings = newsRankingRepository
                .findTop1RankingsSince(since);
        
        return rankings.stream()
                .limit(limit)
                .map(NewsRankingResponse::from)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public List<NewsRankingResponse> getRankingsByDate(Integer sectionId, LocalDate date) {
        List<NewsRanking> rankings = newsRankingRepository
                .findBySectionIdAndDate(sectionId, date.atStartOfDay());

        return rankings.stream()
                .map(NewsRankingResponse::from)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<NewsRankingResponse> getGlobalTopRankings(int limit) {
        LocalDateTime since = LocalDateTime.now().minusHours(24);

        List<NewsRanking> rankings = newsRankingRepository
                .findGlobalTopRankings(since);

        return rankings.stream()
                .limit(limit)
                .map(NewsRankingResponse::from)
                .collect(Collectors.toList());
    }
}