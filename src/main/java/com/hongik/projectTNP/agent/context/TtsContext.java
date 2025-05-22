package com.hongik.projectTNP.agent.context;

import lombok.Builder;
import lombok.Getter;

/**
 * TTS(음성 합성) 기능을 위한 에이전트 컨텍스트
 */
@Getter
@Builder
public class TtsContext implements AgentContext {
    
    private String text;           // 음성으로 변환할 텍스트
    private String voiceId;        // 음성 ID (예: SEOYEON, JOANNA, ...)
    private String language;       // 언어 코드 (ko-KR, en-US, ...)
    private String engine;         // TTS 엔진 유형 (NEURAL, STANDARD)
    private String outputFormat;   // 출력 형식 (MP3, PCM, ...)
    private String audioUrl;       // 생성된 오디오 파일 URL
    private String errorMessage;   // 오류 발생 시 메시지
    
    @Override
    public boolean isValid() {
        return text != null && !text.trim().isEmpty() && 
               voiceId != null && !voiceId.trim().isEmpty();
    }
    
    @Override
    public String getDescription() {
        return String.format("TtsContext(textLength=%d, voiceId=%s, language=%s, engine=%s, format=%s)", 
                text != null ? text.length() : 0, 
                voiceId, 
                language, 
                engine, 
                outputFormat);
    }
    
    /**
     * 오디오 URL을 설정합니다.
     * 
     * @param audioUrl 생성된 오디오 파일 URL
     */
    public void setAudioUrl(String audioUrl) {
        this.audioUrl = audioUrl;
    }
    
    /**
     * 오류 메시지를 설정합니다.
     * 
     * @param errorMessage 오류 메시지
     */
    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
} 