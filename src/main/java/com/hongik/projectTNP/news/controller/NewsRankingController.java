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
    
    @GetMapping("/ranking")
    public ResponseEntity<List<NewsRankingResponse>> getRanking(
            @RequestParam Integer sectionId,
            @RequestParam @DateTimeFormat(pattern = "yyyyMMdd") LocalDate date) {
        
        log.info("뉴스 랭킹 조회 요청 - sectionId: {}, date: {}", sectionId, date);
        
        try {
            List<NewsRankingResponse> rankings = newsRankingService.getRankingsByDate(sectionId, date);
            return ResponseEntity.ok(rankings);
        } catch (Exception e) {
            log.error("뉴스 랭킹 조회 실패 - sectionId: {}, date: {}, error: {}", sectionId, date, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @GetMapping("/ranking/top")
    public ResponseEntity<List<NewsRankingResponse>> getTopRankings(
            @RequestParam(defaultValue = "20") int limit) {
        
        log.info("상위 랭킹 조회 요청 - limit: {}", limit);
        
        try {
            List<NewsRankingResponse> topRankings = newsRankingService.getTop1Rankings(limit);
            return ResponseEntity.ok(topRankings);
        } catch (Exception e) {
            log.error("상위 랭킹 조회 실패 - limit: {}, error: {}", limit, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @GetMapping("/ranking/{id}/content")
    public ResponseEntity<ArticleContentResponse> getArticleContent(@PathVariable Long id) {
        
        log.info("기사 본문 조회 요청 - id: {}", id);
        
        try {
            Optional<ArticleContentResponse> content = newsRankingService.getArticleContent(id);
            
            if (content.isPresent()) {
                return ResponseEntity.ok(content.get());
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("기사 본문 조회 실패 - id: {}, error: {}", id, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
}