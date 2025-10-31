package com.hongik.projectTNP.controller;

import com.hongik.projectTNP.news.dto.SummaryNewsDto;
import com.hongik.projectTNP.service.BookmarkService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bookmark")
@RequiredArgsConstructor
public class BookmarkController {

    private final BookmarkService bookmarkService;

    /**
     * 북마크 추가 (요약 뉴스만 가능)
     * POST /api/bookmark
     */
    @PostMapping
    public ResponseEntity<String> addBookmark(
            Authentication authentication,
            @RequestParam Long summaryNewsCacheId) {
        String username = authentication.getName();
        bookmarkService.addBookmark(username, summaryNewsCacheId);
        return ResponseEntity.ok("북마크가 추가되었습니다.");
    }

    /**
     * 북마크 삭제
     * DELETE /api/bookmark
     */
    @DeleteMapping
    public ResponseEntity<String> removeBookmark(
            Authentication authentication,
            @RequestParam Long summaryNewsCacheId) {
        String username = authentication.getName();
        bookmarkService.removeBookmark(username, summaryNewsCacheId);
        return ResponseEntity.ok("북마크가 삭제되었습니다.");
    }

    /**
     * 북마크 목록 조회
     * GET /api/bookmark
     */
    @GetMapping
    public ResponseEntity<List<SummaryNewsDto>> getBookmarks(Authentication authentication) {
        String username = authentication.getName();
        List<SummaryNewsDto> bookmarks = bookmarkService.getBookmarks(username);
        return ResponseEntity.ok(bookmarks);
    }
}
