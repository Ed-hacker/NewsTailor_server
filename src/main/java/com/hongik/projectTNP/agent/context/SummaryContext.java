package com.hongik.projectTNP.agent.context;

import lombok.Builder;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

/**
 * 뉴스 요약 기능을 위한 에이전트 컨텍스트
 */
@Getter
@Builder
public class SummaryContext implements AgentContext {
    
    private String sourceText;      // 요약할 원본 텍스트
    private String language;        // 요약 언어 (ko, en, ...)
    private Integer maxLength;      // 최대 요약 길이(문자 수)
    private String summaryType;     // 요약 유형 (short, detailed, ...)
    private String summaryText;     // 생성된 요약 텍스트
    private String errorMessage;    // 오류 발생 시 메시지
    
    @Override
    public boolean isValid() {
        return StringUtils.isNotBlank(sourceText) && 
               StringUtils.isNotBlank(language) && 
               maxLength != null && maxLength > 0;
    }
    
    @Override
    public String getDescription() {
        return String.format("SummaryContext(sourceTextLength=%d, language=%s, maxLength=%d, summaryType=%s)", 
                sourceText != null ? sourceText.length() : 0, 
                language, 
                maxLength, 
                summaryType);
    }
    
    /**
     * 요약 결과를 설정합니다.
     * 
     * @param summaryText 생성된 요약 텍스트
     */
    public void setSummaryText(String summaryText) {
        this.summaryText = summaryText;
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