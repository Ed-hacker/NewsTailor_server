package com.hongik.projectTNP.repository;

import com.hongik.projectTNP.domain.SummaryNewsCache;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface SummaryNewsCacheRepository extends JpaRepository<SummaryNewsCache, Long> {

    // 특정 섹션의 최신 요약 뉴스 조회 (rankOrder 순)
    @Query("SELECT s FROM SummaryNewsCache s WHERE s.sectionId = :sectionId " +
           "AND s.generatedAt >= :since ORDER BY s.rankOrder ASC")
    List<SummaryNewsCache> findBySectionIdAndGeneratedAtAfter(
            @Param("sectionId") Integer sectionId,
            @Param("since") LocalDateTime since);

    // 특정 시간 이전 데이터 삭제
    void deleteByGeneratedAtBefore(LocalDateTime dateTime);

    // 특정 섹션의 모든 데이터 삭제 (북마크와 무관하게 모두 삭제)
    void deleteBySectionId(Integer sectionId);

    // 특정 섹션의 데이터 조회
    List<SummaryNewsCache> findBySectionId(Integer sectionId);
}
