package com.hongik.projectTNP.service;

import com.hongik.projectTNP.domain.Summary;
import com.hongik.projectTNP.domain.Tts;

public interface TtsService {
    Tts generateAndUploadTts(Summary summary); // Summary 객체를 받아 TTS 생성 및 업로드 후 Tts 엔티티 반환
    String generateAudio(String textToSynthesize); // 단순히 텍스트를 받아 오디오 URL 반환 (Agent용)

    // TtsRepository에서 제공하는 기능은 여기서 중복 선언 불필요
    // Tts findBySummaryId(Long summaryId);
    // Tts save(Tts tts);
} 