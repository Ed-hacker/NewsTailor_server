package com.hongik.projectTNP.repository;

import com.hongik.projectTNP.domain.Bookmark;
import com.hongik.projectTNP.domain.News;
import com.hongik.projectTNP.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BookmarkRepository extends JpaRepository<Bookmark, Long> {
    Optional<Bookmark> findByUserAndNews(User user, News news);
    List<Bookmark> findByUser(User user);
    List<Bookmark> findByNews(News news);
    void deleteByUserAndNews(User user, News news);
    boolean existsByUserAndNews(User user, News news);
} 