package com.hongik.projectTNP.news.service;

import com.hongik.projectTNP.news.crawler.ArticleContentCrawler;
import com.hongik.projectTNP.news.crawler.NaverRankingCrawler;
import com.hongik.projectTNP.news.crawler.NewsSection;
import com.hongik.projectTNP.news.domain.NewsRanking;
import com.hongik.projectTNP.news.domain.NewsRankingRepository;
import com.hongik.projectTNP.news.dto.ArticleContentResponse;
import com.hongik.projectTNP.news.dto.NewsRankingResponse;
import com.hongik.projectTNP.news.dto.RawArticle;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class NewsRankingService {
    
    private final NewsRankingRepository newsRankingRepository;
    private final NaverRankingCrawler naverRankingCrawler;
    private final ArticleContentCrawler articleContentCrawler;
    
    @Transactional
    public void crawlAndSaveAllSections() {
        // 2일 이전 데이터 삭제
        deleteOldData();

        LocalDate today = LocalDate.now();

        for (NewsSection section : NewsSection.values()) {
            try {
                crawlAndSaveSection(section, today);
            } catch (Exception e) {
                log.error("섹션 크롤링 실패 - Section: {}, Error: {}", section.getSectionName(), e.getMessage());
            }
        }
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
    
    @Transactional
    public void crawlAndSaveSection(NewsSection section, LocalDate date) {
        log.info("섹션 크롤링 시작 - Section: {}, Date: {}", section.getSectionName(), date);
        
        List<RawArticle> rawArticles = naverRankingCrawler.crawlRanking(section, date);
        int savedCount = 0;
        
        for (RawArticle rawArticle : rawArticles) {
            try {
                // 중복 확인
                Optional<NewsRanking> existing = newsRankingRepository.findByUrl(rawArticle.getUrl());
                if (existing.isPresent()) {
                    log.debug("중복 기사 스킵 - URL: {}", rawArticle.getUrl());
                    continue;
                }
                
                // 본문 크롤링
                String body = articleContentCrawler.extractArticleContent(rawArticle.getUrl());
                
                // 엔티티 생성 및 저장
                NewsRanking newsRanking = NewsRanking.builder()
                        .sectionId(rawArticle.getSectionId())
                        .press(rawArticle.getPress())
                        .rank(rawArticle.getRank())
                        .title(rawArticle.getTitle())
                        .url(rawArticle.getUrl())
                        .body(body)
                        .build();
                
                newsRankingRepository.save(newsRanking);
                savedCount++;
                
                log.debug("기사 저장 완료 - Title: {}, Rank: {}", rawArticle.getTitle(), rawArticle.getRank());
                
            } catch (Exception e) {
                log.error("기사 저장 실패 - URL: {}, Error: {}", rawArticle.getUrl(), e.getMessage());
            }
        }
        
        log.info("섹션 크롤링 완료 - Section: {}, 저장된 기사 수: {}/{}", 
                section.getSectionName(), savedCount, rawArticles.size());
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
    public Optional<ArticleContentResponse> getArticleContent(Long id) {
        return newsRankingRepository.findById(id)
                .map(ranking -> ArticleContentResponse.builder()
                        .id(ranking.getId())
                        .title(ranking.getTitle())
                        .url(ranking.getUrl())
                        .body(ranking.getBody())
                        .build());
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