package com.hongik.projectTNP.service.impl;

import com.hongik.projectTNP.domain.*;
import com.hongik.projectTNP.dto.news.NewsAudioResponseDto;
import com.hongik.projectTNP.dto.news.NewsBriefResponseDto;
import com.hongik.projectTNP.dto.news.NewsDetailResponseDto;
import com.hongik.projectTNP.exception.CustomException;
import com.hongik.projectTNP.repository.*;
import com.hongik.projectTNP.service.NewsService;
import com.hongik.projectTNP.service.SummaryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class NewsServiceImpl implements NewsService {

    private static final Logger log = LoggerFactory.getLogger(NewsServiceImpl.class);

    private final NewsRepository newsRepository;
    private final UserRepository userRepository;
    private final UserInterestRepository userInterestRepository;
    private final SummaryRepository summaryRepository;
    private final TtsRepository ttsRepository;
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
    public NewsAudioResponseDto getNewsAudio(Long newsId, String userEmail) {
        News news = newsRepository.findById(newsId)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "뉴스를 찾을 수 없습니다: " + newsId));

        // TtsService가 비활성화되었으므로 임시 응답 또는 null 반환
        // Summary summary = summaryRepository.findByNews(news)
        //         .orElseGet(() -> summaryService.createSummary(news, "기사 요약 요청")); // 요약이 없으면 생성
        //
        // Tts tts = ttsRepository.findBySummary(summary)
        //         .orElseGet(() -> {
        //             // String audioUrl = ttsService.generateAndUploadTts(summary);
        //             // Tts newTts = Tts.builder()
        //             //         .summary(summary)
        //             //         .audioUrl(audioUrl) // 실제 S3 URL 또는 임시 URL
        //             //         .build();
        //             // return ttsRepository.save(newTts);
        //             return Tts.builder().summary(summary).audioUrl("TTS_DISABLED_TEMP_URL").build(); // 임시 데이터
        //         });
        //
        // return new NewsAudioResponseDto(news.getId(), tts.getAudioUrl());

        return new NewsAudioResponseDto(news.getId(), null, "TTS 기능이 현재 비활성화되어 있습니다."); // summaryId에 null 전달
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