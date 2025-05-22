package com.hongik.projectTNP.controller;

import com.hongik.projectTNP.domain.News;
import com.hongik.projectTNP.domain.Summary;
import com.hongik.projectTNP.dto.NewsRequestDto;
import com.hongik.projectTNP.dto.SummaryResponseDto;
import com.hongik.projectTNP.service.NewsService;
import com.hongik.projectTNP.service.SummaryService;
import com.hongik.projectTNP.service.TtsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/news")
public class NewsController {

    private final NewsService newsService;
    private final SummaryService summaryService;
    private final TtsService ttsService;

    @Autowired
    public NewsController(NewsService newsService, SummaryService summaryService, TtsService ttsService) {
        this.newsService = newsService;
        this.summaryService = summaryService;
        this.ttsService = ttsService;
    }

    @GetMapping
    public ResponseEntity<List<SummaryResponseDto>> getAllNews() {
        List<News> newsList = newsService.findAll();
        List<SummaryResponseDto> responseDtos = newsList.stream()
                .map(news -> {
                    Summary summary = summaryService.findByNewsId(news.getId());
                    return SummaryResponseDto.builder()
                            .id(news.getId())
                            .title(news.getTitle())
                            .url(news.getUrl())
                            .summaryText(summary != null ? summary.getText() : null)
                            .audioUrl(summary != null ? summary.getAudioUrl() : null)
                            .publishedAt(news.getPublishedAt())
                            .build();
                })
                .collect(Collectors.toList());
        return ResponseEntity.ok(responseDtos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<SummaryResponseDto> getNewsById(@PathVariable Long id) {
        News news = newsService.findById(id);
        Summary summary = summaryService.findByNewsId(id);

        SummaryResponseDto responseDto = SummaryResponseDto.builder()
                .id(news.getId())
                .title(news.getTitle())
                .url(news.getUrl())
                .content(news.getContent())
                .summaryText(summary != null ? summary.getText() : null)
                .audioUrl(summary != null ? summary.getAudioUrl() : null)
                .publishedAt(news.getPublishedAt())
                .build();

        return ResponseEntity.ok(responseDto);
    }

    @PostMapping("/summarize/{id}")
    public ResponseEntity<SummaryResponseDto> summarizeNews(@PathVariable Long id) {
        News news = newsService.findById(id);
        String summaryText = summaryService.generateSummary(news.getContent());
        String audioUrl = ttsService.generateAudio(summaryText);

        Summary summary = Summary.builder()
                .news(news)
                .text(summaryText)
                .audioUrl(audioUrl)
                .build();

        summaryService.save(summary);

        SummaryResponseDto responseDto = SummaryResponseDto.builder()
                .id(news.getId())
                .title(news.getTitle())
                .summaryText(summaryText)
                .audioUrl(audioUrl)
                .publishedAt(news.getPublishedAt())
                .build();

        return ResponseEntity.ok(responseDto);
    }

    @PostMapping("/custom")
    public ResponseEntity<SummaryResponseDto> processCustomText(@RequestBody NewsRequestDto requestDto) {
        // 사용자 제공 텍스트에 대한 요약 생성
        String summaryText = summaryService.generateSummary(requestDto.getContent());
        String audioUrl = ttsService.generateAudio(summaryText);

        // 임시 응답 생성 (DB에 저장하지 않음)
        SummaryResponseDto responseDto = SummaryResponseDto.builder()
                .title(requestDto.getTitle())
                .content(requestDto.getContent())
                .summaryText(summaryText)
                .audioUrl(audioUrl)
                .build();

        return ResponseEntity.ok(responseDto);
    }
} 