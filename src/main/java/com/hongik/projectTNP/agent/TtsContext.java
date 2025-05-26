package com.hongik.projectTNP.agent;

import com.hongik.projectTNP.domain.Summary;
import lombok.Getter;

@Getter
public class TtsContext {
    private final Summary summaryToConvert; // TTS 변환할 요약 객체
    private final String textToConvert; // 또는 단순히 변환할 텍스트 내용
    // 필요에 따라 TTS 모델에 전달할 추가 파라미터 

    public TtsContext(Summary summaryToConvert) {
        this.summaryToConvert = summaryToConvert;
        this.textToConvert = summaryToConvert.getSummary_text(); // Summary 엔티티의 필드명 확인
    }

    public TtsContext(String textToConvert) {
        this.summaryToConvert = null;
        this.textToConvert = textToConvert;
    }
    // 추가 생성자나 빌더 패턴도 가능
} 