package com.hongik.projectTNP.service;

public interface TtsService {
    
    /**
     * 텍스트를 음성으로 변환하여 URL을 반환합니다.
     * 
     * @param text 음성으로 변환할 텍스트
     * @return 생성된 오디오 파일의 URL
     */
    String generateAudio(String text);
} 