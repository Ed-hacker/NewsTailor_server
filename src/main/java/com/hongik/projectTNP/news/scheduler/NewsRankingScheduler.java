package com.hongik.projectTNP.news.scheduler;

import com.hongik.projectTNP.news.service.NewsRankingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class NewsRankingScheduler {
    
    private final NewsRankingService newsRankingService;
    
    @Scheduled(cron = "0 5 * * * *") // 매 정시 + 5분마다 실행
    public void crawlNewsRankings() {
        log.info("네이버 뉴스 랭킹 스케줄 크롤링 시작");
        
        try {
            newsRankingService.crawlAndSaveAllSections();
            log.info("네이버 뉴스 랭킹 스케줄 크롤링 완료");
        } catch (Exception e) {
            log.error("네이버 뉴스 랭킹 스케줄 크롤링 실패: {}", e.getMessage(), e);
        }
    }
}