package com.hongik.projectTNP.controller;

import com.hongik.projectTNP.dto.interest.InterestResponseDto;
import com.hongik.projectTNP.dto.interest.UserInterestRequestDto;
import com.hongik.projectTNP.dto.news.NewsBriefResponseDto;
import com.hongik.projectTNP.dto.news.NewsDetailResponseDto;
import com.hongik.projectTNP.service.InterestService;
import com.hongik.projectTNP.service.NewsService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.security.Principal; // Spring Security로부터 사용자 정보 주입
import java.util.List;

@RestController
@RequestMapping("/api") // 기본 경로 /api로 통일
@RequiredArgsConstructor
public class NewsController {

    private final NewsService newsService;
    private final InterestService interestService;

    // == News Endpoints ==
    @GetMapping("/news")
    public ResponseEntity<Page<NewsBriefResponseDto>> getPersonalizedNews(
            @AuthenticationPrincipal UserDetails userDetails,
            @PageableDefault(size = 10) Pageable pageable) {
        String username = userDetails.getUsername();
        Page<NewsBriefResponseDto> newsPage = newsService.getPersonalizedNews(username, pageable);
        return ResponseEntity.ok(newsPage);
    }

    @GetMapping("/news/{id}")
    public ResponseEntity<NewsDetailResponseDto> getNewsDetail(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {
        String username = userDetails.getUsername();
        NewsDetailResponseDto newsDetail = newsService.getNewsDetail(id, username);
        return ResponseEntity.ok(newsDetail);
    }


    @PostMapping("/news/{id}/like")
    public ResponseEntity<Void> likeArticle(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {
        String username = userDetails.getUsername();
        newsService.likeArticle(id, username);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/news/{id}/like")
    public ResponseEntity<Void> unlikeArticle(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {
        String username = userDetails.getUsername();
        newsService.unlikeArticle(id, username);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/news/{id}/bookmark")
    public ResponseEntity<Void> bookmarkArticle(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {
        String username = userDetails.getUsername();
        newsService.bookmarkArticle(id, username);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/news/{id}/bookmark")
    public ResponseEntity<Void> unbookmarkArticle(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {
        String username = userDetails.getUsername();
        newsService.unbookmarkArticle(id, username);
        return ResponseEntity.ok().build();
    }
} 