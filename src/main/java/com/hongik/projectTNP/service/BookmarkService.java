package com.hongik.projectTNP.service;

import com.hongik.projectTNP.news.dto.SummaryNewsDto;

import java.util.List;

/**
 * 북마크 관련 비즈니스 로직을 처리하는 서비스 인터페이스
 */
public interface BookmarkService {

    /**
     * 요약 뉴스를 북마크에 추가합니다.
     *
     * @param username 사용자명
     * @param summaryNewsCacheId 요약 뉴스 캐시 ID
     */
    void addBookmark(String username, Long summaryNewsCacheId);

    /**
     * 북마크를 삭제합니다.
     *
     * @param username 사용자명
     * @param summaryNewsCacheId 요약 뉴스 캐시 ID
     */
    void removeBookmark(String username, Long summaryNewsCacheId);

    /**
     * 사용자의 북마크 목록을 조회합니다.
     *
     * @param username 사용자명
     * @return 북마크된 요약 뉴스 목록
     */
    List<SummaryNewsDto> getBookmarks(String username);
}
