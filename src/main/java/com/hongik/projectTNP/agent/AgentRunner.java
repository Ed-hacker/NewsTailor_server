package com.hongik.projectTNP.agent;

import com.hongik.projectTNP.agent.context.AgentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * MCP 아키텍처의 에이전트 실행을 위한 추상 클래스
 * 모든 에이전트 실행기는 이 클래스를 상속받아 구현해야 함
 */
public abstract class AgentRunner<T extends AgentContext> {
    
    private static final Logger logger = LoggerFactory.getLogger(AgentRunner.class);
    
    /**
     * 에이전트 실행에 필요한 컨텍스트를 초기화합니다.
     * 
     * @param parameters 컨텍스트 초기화에 필요한 파라미터
     * @return 초기화된 에이전트 컨텍스트
     */
    public abstract T initializeContext(Object... parameters);
    
    /**
     * 에이전트를 실행하고 결과를 반환합니다.
     * 
     * @param context 에이전트 실행에 필요한 컨텍스트
     * @return 에이전트 실행 결과
     */
    public abstract Object execute(T context);
    
    /**
     * 에이전트 실행 후 필요한 정리 작업을 수행합니다.
     * 
     * @param context 에이전트 컨텍스트
     */
    public void cleanup(T context) {
        logger.info("에이전트 실행이 완료되었습니다.");
    }
    
    /**
     * 에이전트의 전체 실행 흐름을 관리합니다.
     * 
     * @param parameters 에이전트 실행에 필요한 파라미터
     * @return 에이전트 실행 결과
     */
    public final Object run(Object... parameters) {
        T context = null;
        try {
            // 1. 컨텍스트 초기화
            context = initializeContext(parameters);
            logger.info("에이전트 컨텍스트가 초기화되었습니다: {}", context);
            
            // 2. 에이전트 실행
            Object result = execute(context);
            logger.info("에이전트가 성공적으로 실행되었습니다.");
            
            return result;
        } catch (Exception e) {
            logger.error("에이전트 실행 중 오류가 발생했습니다: {}", e.getMessage(), e);
            throw new RuntimeException("에이전트 실행 실패", e);
        } finally {
            // 3. 정리 작업
            if (context != null) {
                cleanup(context);
            }
        }
    }
} 