package com.hongik.projectTNP.controller;

import com.hongik.projectTNP.news.dto.SummaryNewsDto;
import com.hongik.projectTNP.news.service.NewsSelectionService;
import com.hongik.projectTNP.service.SummaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/test")
@RequiredArgsConstructor
public class TestController {

    private final SummaryService summaryService;
    private final NewsSelectionService newsSelectionService;

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
}