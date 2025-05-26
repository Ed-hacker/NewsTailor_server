package com.hongik.projectTNP.agent;

// T는 Context 타입 
// R은 실행 결과 타입 
public interface AgentRunner<T, R> {
    R run(T context);
} 