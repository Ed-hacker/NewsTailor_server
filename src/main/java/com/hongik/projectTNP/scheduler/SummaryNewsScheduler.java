package com.hongik.projectTNP.scheduler;

import com.hongik.projectTNP.service.NewsRankingService;
import com.hongik.projectTNP.service.NewsSelectionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class SummaryNewsScheduler {

    private final NewsRankingService newsRankingService;
    private final NewsSelectionService newsSelectionService;

    /**
     * 하루 3번 요약 뉴스 생성 (06:00, 11:00, 17:00)
     * 1. 최신 랭킹 뉴스 크롤링
     * 2. 요약 뉴스 생성 및 캐시
     * cron: 초 분 시 일 월 요일
     */
    @Scheduled(cron = "0 0 6,11,17 * * *")
    public void generateSummaryNews() {
        log.info("===== 요약 뉴스 스케줄 시작 =====");

        try {
            // 1. 최신 랭킹 뉴스 크롤링
            log.info("1단계: 랭킹 뉴스 크롤링 시작");
            newsRankingService.crawlAndSaveAllSections();
            log.info("1단계: 랭킹 뉴스 크롤링 완료");

            // 2. 요약 뉴스 생성 및 캐시
            log.info("2단계: 요약 뉴스 생성 시작");
            newsSelectionService.generateAndCacheAllSections();
            log.info("2단계: 요약 뉴스 생성 완료");

            log.info("===== 요약 뉴스 스케줄 완료 =====");
        } catch (Exception e) {
            log.error("요약 뉴스 스케줄 실패: {}", e.getMessage(), e);
        }
    }
}
