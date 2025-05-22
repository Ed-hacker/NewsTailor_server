package com.hongik.projectTNP.service;

import com.hongik.projectTNP.domain.News;
import com.hongik.projectTNP.repository.NewsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 뉴스 관련 비즈니스 로직을 처리하는 서비스 구현체
 */
@Service
public class NewsServiceImpl implements NewsService {

    private final NewsRepository newsRepository;

    @Autowired
    public NewsServiceImpl(NewsRepository newsRepository) {
        this.newsRepository = newsRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<News> findAll() {
        return newsRepository.findAllByOrderByPublishedAtDesc();
    }

    @Override
    @Transactional(readOnly = true)
    public News findById(Long id) {
        return newsRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("뉴스를 찾을 수 없습니다. ID: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<News> findByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return newsRepository.findByPublishedAtBetweenOrderByPublishedAtDesc(startDate, endDate);
    }

    @Override
    @Transactional
    public News save(News news) {
        return newsRepository.save(news);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByUrl(String url) {
        return newsRepository.existsByUrl(url);
    }

} 