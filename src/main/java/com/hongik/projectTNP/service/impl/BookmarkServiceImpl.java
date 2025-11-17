package com.hongik.projectTNP.service.impl;

import com.hongik.projectTNP.domain.Bookmark;
import com.hongik.projectTNP.domain.User;
import com.hongik.projectTNP.news.domain.SummaryNewsCache;
import com.hongik.projectTNP.news.domain.SummaryNewsCacheRepository;
import com.hongik.projectTNP.news.dto.SummaryNewsDto;
import com.hongik.projectTNP.repository.BookmarkRepository;
import com.hongik.projectTNP.repository.UserRepository;
import com.hongik.projectTNP.service.BookmarkService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookmarkServiceImpl implements BookmarkService {

    private static final int MAX_BOOKMARKS_PER_USER = 50;

    private final BookmarkRepository bookmarkRepository;
    private final UserRepository userRepository;
    private final SummaryNewsCacheRepository summaryNewsCacheRepository;

    @Override
    @Transactional
    public void addBookmark(String username, Long summaryNewsCacheId) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + username));

        // 북마크 개수 제한 확인 (최대 50개)
        long currentBookmarkCount = bookmarkRepository.countByUser(user);
        if (currentBookmarkCount >= MAX_BOOKMARKS_PER_USER) {
            throw new IllegalStateException("북마크는 최대 " + MAX_BOOKMARKS_PER_USER + "개까지만 추가할 수 있습니다.");
        }

        SummaryNewsCache summaryNewsCache = summaryNewsCacheRepository.findById(summaryNewsCacheId)
                .orElseThrow(() -> new IllegalArgumentException("요약 뉴스를 찾을 수 없습니다: " + summaryNewsCacheId));

        // 이미 북마크가 있는지 확인 (URL 기준)
        if (bookmarkRepository.existsByUserAndUrl(user, summaryNewsCache.getUrl())) {
            throw new IllegalStateException("이미 북마크된 뉴스입니다.");
        }

        // 뉴스 정보를 복사해서 저장 (캐시가 삭제되어도 북마크는 유지)
        Bookmark bookmark = Bookmark.builder()
                .user(user)
                .sectionId(summaryNewsCache.getSectionId())
                .sectionName(summaryNewsCache.getSectionName())
                .title(summaryNewsCache.getTitle())
                .url(summaryNewsCache.getUrl())
                .summary(summaryNewsCache.getSummary())
                .build();

        bookmarkRepository.save(bookmark);
        log.info("북마크 추가 완료 - User: {}, Title: {}", username, summaryNewsCache.getTitle());
    }

    @Override
    @Transactional
    public void removeBookmark(String username, Long summaryNewsCacheId) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + username));

        // summaryNewsCacheId는 실제로 bookmarkId를 의미 (프론트엔드에서 getBookmarks로 받은 id 값)
        Bookmark bookmark = bookmarkRepository.findById(summaryNewsCacheId)
                .orElseThrow(() -> new IllegalArgumentException("북마크를 찾을 수 없습니다: " + summaryNewsCacheId));

        // 본인의 북마크인지 확인
        if (!bookmark.getUser().equals(user)) {
            throw new IllegalArgumentException("본인의 북마크만 삭제할 수 있습니다.");
        }

        bookmarkRepository.delete(bookmark);
        log.info("북마크 삭제 완료 - User: {}, BookmarkId: {}", username, summaryNewsCacheId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SummaryNewsDto> getBookmarks(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + username));

        List<Bookmark> bookmarks = bookmarkRepository.findByUserOrderByCreatedAtDesc(user);

        return bookmarks.stream()
                .map(bookmark -> SummaryNewsDto.builder()
                        .id(bookmark.getId())
                        .sectionId(bookmark.getSectionId())
                        .sectionName(bookmark.getSectionName())
                        .title(bookmark.getTitle())
                        .url(bookmark.getUrl())
                        .summary(bookmark.getSummary())
                        .build())
                .collect(Collectors.toList());
    }
}
