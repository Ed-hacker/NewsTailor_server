package com.hongik.projectTNP.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "bookmark")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Bookmark {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // 뉴스 정보를 직접 저장 (캐시가 삭제되어도 북마크는 유지)
    @Column(nullable = false)
    private Integer sectionId;

    @Column(length = 50, nullable = false)
    private String sectionName;

    @Column(length = 300, nullable = false)
    private String title;

    @Column(length = 500, nullable = false)
    private String url;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String summary;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
} 