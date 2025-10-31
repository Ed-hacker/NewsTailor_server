package com.hongik.projectTNP.repository;

import com.hongik.projectTNP.domain.Bookmark;
import com.hongik.projectTNP.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BookmarkRepository extends JpaRepository<Bookmark, Long> {
    List<Bookmark> findByUser(User user);
    List<Bookmark> findByUserOrderByCreatedAtDesc(User user);
    Optional<Bookmark> findByUserAndUrl(User user, String url);
    boolean existsByUserAndUrl(User user, String url);
    void deleteByUserAndUrl(User user, String url);
    long countByUser(User user);
} 