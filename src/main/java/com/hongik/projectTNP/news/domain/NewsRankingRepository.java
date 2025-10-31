package com.hongik.projectTNP.news.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface  NewsRankingRepository extends JpaRepository<NewsRanking, Long> {
    
    Optional<NewsRanking> findByUrl(String url);
    
    List<NewsRanking> findBySectionIdAndCollectedAtBetweenOrderByRankAsc(
        Integer sectionId, 
        LocalDateTime startDate, 
        LocalDateTime endDate
    );
    
    @Query("SELECT n FROM NewsRanking n WHERE n.rank = 1 AND n.collectedAt >= :since ORDER BY n.collectedAt DESC")
    List<NewsRanking> findTop1RankingsSince(@Param("since") LocalDateTime since);
    
    @Query("SELECT n FROM NewsRanking n WHERE n.sectionId = :sectionId AND DATE(n.collectedAt) = DATE(:date) ORDER BY n.rank ASC")
    List<NewsRanking> findBySectionIdAndDate(@Param("sectionId") Integer sectionId, @Param("date") LocalDateTime date);

    @Query("SELECT n FROM NewsRanking n WHERE n.collectedAt >= :since ORDER BY n.rank ASC, n.collectedAt DESC")
    List<NewsRanking> findGlobalTopRankings(@Param("since") LocalDateTime since);

    void deleteByCollectedAtBefore(LocalDateTime dateTime);
}