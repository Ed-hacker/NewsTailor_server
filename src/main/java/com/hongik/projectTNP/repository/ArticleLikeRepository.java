package com.hongik.projectTNP.repository;

import com.hongik.projectTNP.domain.ArticleLike;
import com.hongik.projectTNP.domain.News;
import com.hongik.projectTNP.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ArticleLikeRepository extends JpaRepository<ArticleLike, Long> {
    Optional<ArticleLike> findByUserAndNews(User user, News news);
    List<ArticleLike> findByUser(User user);
    List<ArticleLike> findByNews(News news);
    void deleteByUserAndNews(User user, News news);
    boolean existsByUserAndNews(User user, News news);
    long countByNews(News news); // 특정 뉴스의 좋아요 개수
} 