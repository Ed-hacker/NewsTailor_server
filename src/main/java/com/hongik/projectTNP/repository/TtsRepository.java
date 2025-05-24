package com.hongik.projectTNP.repository;

import com.hongik.projectTNP.domain.Summary;
import com.hongik.projectTNP.domain.Tts;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TtsRepository extends JpaRepository<Tts, Long> {
    Optional<Tts> findBySummary(Summary summary);
    Optional<Tts> findBySummaryId(Long summaryId);
} 