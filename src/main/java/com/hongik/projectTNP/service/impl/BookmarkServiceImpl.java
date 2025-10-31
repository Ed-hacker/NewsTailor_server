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
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookmarkServiceImpl implements BookmarkService {

    private final BookmarkRepository bookmarkRepository;
    private final UserRepository userRepository;
    private final SummaryNewsCacheRepository summaryNewsCacheRepository;

    @Override
    @Transactional
    public void addBookmark(String username, Long summaryNewsCacheId) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + username));

        SummaryNewsCache summaryNewsCache = summaryNewsCacheRepository.findById(summaryNewsCacheId)
                .orElseThrow(() -> new IllegalArgumentException("요약 뉴스를 찾을 수 없습니다: " + summaryNewsCacheId));

        // 이미 북마크가 있는지 확인
        if (bookmarkRepository.existsByUserAndSummaryNewsCache(user, summaryNewsCache)) {
            throw new IllegalStateException("이미 북마크된 뉴스입니다.");
        }

        Bookmark bookmark = Bookmark.builder()
                .user(user)
                .summaryNewsCache(summaryNewsCache)
                .build();

        bookmarkRepository.save(bookmark);
    }

    @Override
    @Transactional
    public void removeBookmark(String username, Long summaryNewsCacheId) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + username));

        SummaryNewsCache summaryNewsCache = summaryNewsCacheRepository.findById(summaryNewsCacheId)
                .orElseThrow(() -> new IllegalArgumentException("요약 뉴스를 찾을 수 없습니다: " + summaryNewsCacheId));

        bookmarkRepository.deleteByUserAndSummaryNewsCache(user, summaryNewsCache);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SummaryNewsDto> getBookmarks(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + username));

        List<Bookmark> bookmarks = bookmarkRepository.findByUser(user);

        return bookmarks.stream()
                .map(bookmark -> {
                    SummaryNewsCache cache = bookmark.getSummaryNewsCache();
                    return SummaryNewsDto.builder()
                            .id(cache.getId())
                            .sectionId(cache.getSectionId())
                            .sectionName(cache.getSectionName())
                            .title(cache.getTitle())
                            .url(cache.getUrl())
                            .summary(cache.getSummary())
                            .rankOrder(cache.getRankOrder())
                            .build();
                })
                .collect(Collectors.toList());
    }
}
