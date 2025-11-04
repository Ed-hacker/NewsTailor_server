package com.hongik.projectTNP.news.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "news_rankings", 
       indexes = {
           @Index(name = "idx_section_rank", columnList = "sectionId, `rank`"),
           @Index(name = "idx_collected_at", columnList = "collectedAt"),
           @Index(name = "idx_url", columnList = "url", unique = true)
       })
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NewsRanking {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private Integer sectionId;
    
    @Column(length = 50, nullable = false)
    private String press;
    
    @Column(name = "`rank`", nullable = false)
    private Integer rank;
    
    @Column(length = 300, nullable = false)
    private String title;
    
    @Column(length = 500, nullable = false, unique = true)
    private String url;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String summary;  // Gemini가 생성한 요약 (본문은 저장하지 않음)

    @Column(nullable = false)
    private LocalDateTime collectedAt;

    @PrePersist
    protected void onCreate() {
        collectedAt = LocalDateTime.now();
    }
}