package com.hongik.projectTNP.repository;

import com.hongik.projectTNP.domain.Bookmark;
import com.hongik.projectTNP.domain.User;
import com.hongik.projectTNP.news.domain.SummaryNewsCache;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BookmarkRepository extends JpaRepository<Bookmark, Long> {
    Optional<Bookmark> findByUserAndSummaryNewsCache(User user, SummaryNewsCache summaryNewsCache);
    List<Bookmark> findByUser(User user);
    List<Bookmark> findBySummaryNewsCache(SummaryNewsCache summaryNewsCache);
    void deleteByUserAndSummaryNewsCache(User user, SummaryNewsCache summaryNewsCache);
    boolean existsByUserAndSummaryNewsCache(User user, SummaryNewsCache summaryNewsCache);
} 