package com.hongik.projectTNP.controller;

import com.hongik.projectTNP.news.dto.SummaryNewsDto;
import com.hongik.projectTNP.news.service.NewsRankingService;
import com.hongik.projectTNP.news.service.NewsSelectionService;
import com.hongik.projectTNP.service.SummaryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/test")
@RequiredArgsConstructor
public class TestController {

    private final SummaryService summaryService;
    private final NewsSelectionService newsSelectionService;
    private final NewsRankingService newsRankingService;

    @PostMapping("/summary")
    public ResponseEntity<String> testSummary(@RequestBody String text) {
        String summary = summaryService.generateSummary(text);
        return ResponseEntity.ok(summary);
    }

    /**
     * Gemini 연결 테스트 - 특정 섹션의 뉴스 4개 선별 및 요약
     * GET /api/test/gemini?sectionId=100
     */
    @GetMapping("/gemini")
    public ResponseEntity<?> testGemini(@RequestParam(value = "sectionId", defaultValue = "100") Integer sectionId) {
        try {
            List<SummaryNewsDto> result = newsSelectionService.selectAndSummarizeTop4(sectionId);

            if (result.isEmpty()) {
                return ResponseEntity.ok("뉴스가 없거나 Gemini 응답이 비어있습니다.");
            }

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body("Gemini 테스트 실패: " + e.getMessage());
        }
    }

    /**
     * 전체 요약 뉴스 생성 및 캐시 테스트
     * POST /api/test/generate-all
     */
    @PostMapping("/generate-all")
    public ResponseEntity<String> testGenerateAll() {
        try {
            newsSelectionService.generateAndCacheAllSections();
            return ResponseEntity.ok("전체 섹션 요약 뉴스 생성 완료");
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body("생성 실패: " + e.getMessage());
        }
    }

    /**
     * 전체 프로세스 실행: 크롤링 → 요약 → 캐시 저장
     * DB가 비어있어도 이 API 한번 호출하면 모든 데이터가 준비됩니다.
     * POST /api/test/full-process
     */
    @PostMapping("/full-process")
    public ResponseEntity<String> runFullProcess() {
        log.info("===== 전체 프로세스 시작 (크롤링 + 요약 + 캐시) =====");

        try {
            // 1. 랭킹 뉴스 크롤링 (하이브리드 알고리즘 포함)
            log.info("1단계: 랭킹 뉴스 크롤링 + 하이브리드 알고리즘 시작");
            newsRankingService.crawlAndSaveAllSections();
            log.info("1단계: 랭킹 뉴스 크롤링 완료");

            // 2. 요약 뉴스 생성 및 캐시
            log.info("2단계: 요약 뉴스 생성 및 캐시 시작");
            newsSelectionService.generateAndCacheAllSections();
            log.info("2단계: 요약 뉴스 생성 및 캐시 완료");

            log.info("===== 전체 프로세스 완료 =====");
            return ResponseEntity.ok(
                "✅ 전체 프로세스 완료!\n" +
                "1. 랭킹 뉴스 하이브리드 알고리즘 적용 완료 (상위 20개 선정)\n" +
                "2. 요약 뉴스 생성 및 캐시 저장 완료\n" +
                "이제 맞춤 요약 뉴스 및 랭킹 뉴스 API를 사용할 수 있습니다."
            );
        } catch (Exception e) {
            log.error("전체 프로세스 실패: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body("❌ 전체 프로세스 실패: " + e.getMessage());
        }
    }

    /**
     * 하이브리드 랭킹 알고리즘 테스트
     * POST /api/test/ranking-algorithm
     */
    @PostMapping("/ranking-algorithm")
    public ResponseEntity<String> testRankingAlgorithm() {
        log.info("===== 하이브리드 랭킹 알고리즘 테스트 시작 =====");

        try {
            newsRankingService.crawlAndSaveAllSections();

            return ResponseEntity.ok(
                "✅ 하이브리드 랭킹 알고리즘 테스트 완료!\n" +
                "Phase 1: 키워드 클러스터링 완료\n" +
                "Phase 2: Gemini 기반 최종 선정 완료\n" +
                "상위 20개 랭킹 뉴스가 DB에 저장되었습니다.\n" +
                "GET /api/v1/news/ranking 으로 확인하세요."
            );
        } catch (Exception e) {
            log.error("랭킹 알고리즘 테스트 실패: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body("❌ 랭킹 알고리즘 테스트 실패: " + e.getMessage());
        }
    }
}