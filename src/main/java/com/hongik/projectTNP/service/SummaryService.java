package com.hongik.projectTNP.service;

import com.hongik.projectTNP.domain.News;
import com.hongik.projectTNP.domain.Summary;

public interface SummaryService {
    Summary summarizeNews(News news);
    String generateSummary(String textToSummarize);
} 