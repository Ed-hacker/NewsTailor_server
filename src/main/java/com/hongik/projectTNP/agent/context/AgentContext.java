package com.hongik.projectTNP.agent.context;

/**
 * MCP 아키텍처에서 에이전트 실행에 필요한 컨텍스트 인터페이스
 * 모든 에이전트 컨텍스트는 이 인터페이스를 구현해야 함
 */
public interface AgentContext {
    
    /**
     * 컨텍스트가 유효한지 확인합니다.
     * 
     * @return 컨텍스트 유효성 여부
     */
    boolean isValid();
    
    /**
     * 컨텍스트에 대한 설명을 반환합니다.
     * 
     * @return 컨텍스트 설명
     */
    String getDescription();
} 