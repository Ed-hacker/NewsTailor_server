package com.hongik.projectTNP.repository;

import com.hongik.projectTNP.domain.Summary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SummaryRepository extends JpaRepository<Summary, Long> {
    
    /**
     * 뉴스 ID에 해당하는 요약을 조회합니다.
     * 
     * @param newsId 뉴스 ID
     * @return 해당 뉴스의 요약 정보
     */
    Summary findByNewsId(Long newsId);
    
    /**
     * 뉴스 ID에 해당하는 요약이 존재하는지 확인합니다.
     * 
     * @param newsId 뉴스 ID
     * @return 요약 존재 여부
     */
    boolean existsByNewsId(Long newsId);
    
} 