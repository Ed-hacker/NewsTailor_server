package com.hongik.projectTNP.dto.news;

import com.hongik.projectTNP.domain.Tts;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class NewsAudioResponseDto {
    private Long newsId;
    private Long summaryId;
    private String audioUrl;

    public static NewsAudioResponseDto from(Tts tts) {
        return new NewsAudioResponseDto(tts.getSummary().getNews().getId(), tts.getSummary().getId(), tts.getAudioUrl());
    }
} 