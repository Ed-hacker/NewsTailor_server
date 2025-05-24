package com.hongik.projectTNP.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;

@Entity
@Table(name = "summary")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Summary {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "news_id", nullable = false, unique = true)
    private News news;

    @Column(columnDefinition = "TEXT")
    private String summary_text; // 변경: content -> summary_text
} 