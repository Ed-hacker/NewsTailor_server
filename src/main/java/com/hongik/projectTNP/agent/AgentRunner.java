package com.hongik.projectTNP.agent;

// T는 Context 타입 (예: SummaryContext, TtsContext)
// R은 실행 결과 타입 (예: String 요약 결과, String 오디오 URL)
public interface AgentRunner<T, R> {
    R run(T context);
} 