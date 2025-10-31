package com.hongik.projectTNP.news.controller;

import com.hongik.projectTNP.news.dto.ArticleContentResponse;
import com.hongik.projectTNP.news.dto.NewsRankingResponse;
import com.hongik.projectTNP.news.service.NewsRankingService;
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
     * 글로벌 TOP 20 랭킹 뉴스 조회 (요약 포함)
     * GET /api/v1/news/ranking/global/top20
     */
    @GetMapping("/ranking/global/top20")
    public ResponseEntity<List<NewsRankingResponse>> getGlobalTop20() {

        log.info("글로벌 TOP 20 랭킹 조회 요청");

        try {
            List<NewsRankingResponse> topRankings = newsRankingService.getGlobalTopRankings(20);
            return ResponseEntity.ok(topRankings);
        } catch (Exception e) {
            log.error("글로벌 TOP 20 랭킹 조회 실패 - error: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
}