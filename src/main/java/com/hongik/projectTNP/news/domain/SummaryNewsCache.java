package com.hongik.projectTNP.news.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "summary_news_cache",
       indexes = {
           @Index(name = "idx_section_rank", columnList = "sectionId, rankOrder"),
           @Index(name = "idx_generated_at", columnList = "generatedAt")
       })
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SummaryNewsCache {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Integer sectionId;

    @Column(length = 100, nullable = false)
    private String sectionName;

    @Column(length = 300, nullable = false)
    private String title;

    @Column(length = 500, nullable = false)
    private String url;

    @Lob
    @Column(columnDefinition = "TEXT", nullable = false)
    private String summary;

    @Column(nullable = false)
    private Integer rankOrder;  // 1~4

    @Column(nullable = false)
    private LocalDateTime generatedAt;

    @PrePersist
    protected void onCreate() {
        generatedAt = LocalDateTime.now();
    }
}
