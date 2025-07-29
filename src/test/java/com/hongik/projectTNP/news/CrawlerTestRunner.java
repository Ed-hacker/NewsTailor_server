package com.hongik.projectTNP.news;

import com.hongik.projectTNP.news.crawler.NaverRankingCrawler;
import com.hongik.projectTNP.news.crawler.NewsSection;
import com.hongik.projectTNP.news.dto.RawArticle;

import java.time.LocalDate;
import java.util.List;

public class CrawlerTestRunner {
    public static void main(String[] args) {
        NaverRankingCrawler crawler = new NaverRankingCrawler();
        
        System.out.println("=== 네이버 뉴스 랭킹 크롤링 테스트 ===");
        
        LocalDate today = LocalDate.now();
        NewsSection section = NewsSection.IT_SCIENCE;
        
        System.out.println("섹션: " + section.getSectionName());
        System.out.println("날짜: " + today);
        System.out.println();
        
        List<RawArticle> articles = crawler.crawlRanking(section, today);
        
        System.out.println("크롤링 결과: " + articles.size() + "개 기사");
        System.out.println();
        
        for (int i = 0; i < Math.min(3, articles.size()); i++) {
            RawArticle article = articles.get(i);
            System.out.println((i+1) + ". [" + article.getRank() + "위] " + article.getTitle());
            System.out.println("   언론사: " + article.getPress());
            System.out.println("   URL: " + article.getUrl());
            System.out.println();
        }
    }
}