package com.hongik.projectTNP.service;

import com.hongik.projectTNP.domain.SummaryNewsCache;
import com.hongik.projectTNP.repository.SummaryNewsCacheRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SummaryNewsReadService {

    private final SummaryNewsCacheRepository summaryNewsCacheRepository;

    /**
     * 섹션별 요약 뉴스 조회 (Caffeine 캐시 적용)
     * 캐시 키: sectionId
     */
    @Cacheable(value = "summaryNews", key = "#sectionId")
    public List<SummaryNewsCache> findBySectionId(Integer sectionId) {
        LocalDateTime since = LocalDateTime.now().minusHours(12);
        log.info("DB에서 요약 뉴스 조회 - sectionId: {} (캐시 미스)", sectionId);
        return summaryNewsCacheRepository.findBySectionIdAndGeneratedAtAfter(sectionId, since);
    }

    /**
     * 스케줄러에서 새 데이터 생성 후 캐시 전체 초기화
     */
    @CacheEvict(value = "summaryNews", allEntries = true)
    public void evictAllCache() {
        log.info("summaryNews 캐시 전체 초기화");
    }
}
