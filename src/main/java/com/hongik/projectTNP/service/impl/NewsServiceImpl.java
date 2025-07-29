package com.hongik.projectTNP.service.impl;

import com.hongik.projectTNP.domain.*;
import com.hongik.projectTNP.dto.news.NewsBriefResponseDto;
import com.hongik.projectTNP.dto.news.NewsDetailResponseDto;
import com.hongik.projectTNP.exception.CustomException;
import com.hongik.projectTNP.repository.*;
import com.hongik.projectTNP.service.NewsService;
import com.hongik.projectTNP.service.SummaryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class NewsServiceImpl implements NewsService {


    private final NewsRepository newsRepository;
    private final UserRepository userRepository;
    private final UserInterestRepository userInterestRepository;
    private final SummaryRepository summaryRepository;
    private final ArticleLikeRepository articleLikeRepository;
    private final BookmarkRepository bookmarkRepository;
    private final SummaryService summaryService;

    @Override
    @Transactional(readOnly = true)
    public Page<NewsBriefResponseDto> getPersonalizedNews(String userEmail, Pageable pageable) {
        User user = userRepository.findByUsername(userEmail)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다: " + userEmail));

        List<UserInterest> userInterests = userInterestRepository.findByUser(user);

        Page<News> newsPage;
        if (userInterests.isEmpty()) {
            log.info("사용자 {}의 관심사가 설정되지 않았습니다. 모든 뉴스를 최신순으로 반환합니다.", userEmail);
            newsPage = newsRepository.findAllByOrderByPublishedAtDesc(pageable);
        } else {
            Set<String> categories = userInterests.stream()
                    .map(userInterest -> userInterest.getInterest().getName())
                    .collect(Collectors.toSet());
            log.info("사용자 {}의 관심사 카테고리 기반 뉴스 조회: {}", userEmail, categories);
            newsPage = newsRepository.findByCategoryInOrderByPublishedAtDesc(categories, pageable);
        }

        List<NewsBriefResponseDto> dtos = newsPage.getContent().stream()
                .map(NewsBriefResponseDto::from)
                .collect(Collectors.toList());
        return new PageImpl<>(dtos, pageable, newsPage.getTotalElements());
    }

    @Override
    @Transactional(readOnly = true)
    public NewsDetailResponseDto getNewsDetail(Long newsId, String userEmail) {
        User user = userRepository.findByUsername(userEmail)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다: " + userEmail));
        News news = newsRepository.findById(newsId)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "뉴스를 찾을 수 없습니다: " + newsId));
        Summary summary = summaryRepository.findByNewsId(newsId).orElse(null);
        // TODO: Summary가 null일 경우 SummaryService.summarizeNews(news) 호출하여 생성하는 로직 고려

        boolean isLiked = articleLikeRepository.existsByUserAndNews(user, news);
        boolean isBookmarked = bookmarkRepository.existsByUserAndNews(user, news);
        long likeCount = articleLikeRepository.countByNews(news);

        return NewsDetailResponseDto.from(news, summary, isLiked, isBookmarked, likeCount);
    }


    @Override
    @Transactional
    public void likeArticle(Long newsId, String userEmail) {
        User user = userRepository.findByUsername(userEmail)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다: " + userEmail));
        News news = newsRepository.findById(newsId)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "뉴스를 찾을 수 없습니다: " + newsId));

        if (!articleLikeRepository.existsByUserAndNews(user, news)) {
            ArticleLike articleLike = ArticleLike.builder().user(user).news(news).build();
            articleLikeRepository.save(articleLike);
        }
    }

    @Override
    @Transactional
    public void unlikeArticle(Long newsId, String userEmail) {
        User user = userRepository.findByUsername(userEmail)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다: " + userEmail));
        News news = newsRepository.findById(newsId)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "뉴스를 찾을 수 없습니다: " + newsId));
        articleLikeRepository.findByUserAndNews(user, news)
                .ifPresent(articleLikeRepository::delete);
    }

    @Override
    @Transactional
    public void bookmarkArticle(Long newsId, String userEmail) {
        User user = userRepository.findByUsername(userEmail)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다: " + userEmail));
        News news = newsRepository.findById(newsId)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "뉴스를 찾을 수 없습니다: " + newsId));

        if (!bookmarkRepository.existsByUserAndNews(user, news)) {
            Bookmark bookmark = Bookmark.builder().user(user).news(news).build();
            bookmarkRepository.save(bookmark);
        }
    }

    @Override
    @Transactional
    public void unbookmarkArticle(Long newsId, String userEmail) {
        User user = userRepository.findByUsername(userEmail)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다: " + userEmail));
        News news = newsRepository.findById(newsId)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "뉴스를 찾을 수 없습니다: " + newsId));
        bookmarkRepository.findByUserAndNews(user, news)
                .ifPresent(bookmarkRepository::delete);
    }
} 