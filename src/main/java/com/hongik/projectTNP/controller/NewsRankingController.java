package com.hongik.projectTNP.controller;

import com.hongik.projectTNP.dto.news.NewsRankingResponse;
import com.hongik.projectTNP.service.NewsRankingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/api/v1/news")
@RequiredArgsConstructor
public class NewsRankingController {

    private final NewsRankingService newsRankingService;

    /**
     * 랭킹 뉴스 조회 (요약 포함)
     * GET /api/v1/news/ranking
     */
    @GetMapping("/ranking")
    public ResponseEntity<List<NewsRankingResponse>> getRankingNews() {

        log.info("랭킹 뉴스 조회 요청");

        try {
            List<NewsRankingResponse> topRankings = newsRankingService.getGlobalTopRankings(20);
            return ResponseEntity.ok(topRankings);
        } catch (Exception e) {
            log.error("랭킹 뉴스 조회 실패 - error: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
}