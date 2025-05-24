package com.hongik.projectTNP.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "article_like", // 'like'는 SQL 예약어일 수 있으므로 변경
       uniqueConstraints = {
           @UniqueConstraint(columnNames = {"user_id", "news_id"})
       })
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ArticleLike { // 클래스명도 'Like' 대신 'ArticleLike'로 변경

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "news_id", nullable = false)
    private News news;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
} 