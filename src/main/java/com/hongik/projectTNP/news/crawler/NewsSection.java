package com.hongik.projectTNP.news.crawler;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum NewsSection {
    POLITICS(100, "정치"),
    ECONOMY(101, "경제"),
    SOCIETY(102, "사회"),
    LIFE_CULTURE(103, "생활/문화"),
    WORLD(104, "세계"),
    IT_SCIENCE(105, "IT/과학");
    
    private final int sectionId;
    private final String sectionName;
    
    public static NewsSection fromSectionId(int sectionId) {
        for (NewsSection section : values()) {
            if (section.sectionId == sectionId) {
                return section;
            }
        }
        throw new IllegalArgumentException("Invalid section ID: " + sectionId);
    }
}