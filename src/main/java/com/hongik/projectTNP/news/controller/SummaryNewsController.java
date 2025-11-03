package com.hongik.projectTNP.news.controller;

import com.hongik.projectTNP.domain.User;
import com.hongik.projectTNP.domain.UserInterest;
import com.hongik.projectTNP.exception.CustomException;
import com.hongik.projectTNP.news.domain.SummaryNewsCache;
import com.hongik.projectTNP.news.domain.SummaryNewsCacheRepository;
import com.hongik.projectTNP.news.dto.SummaryNewsDto;
import com.hongik.projectTNP.repository.UserInterestRepository;
import com.hongik.projectTNP.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/v1/summary-news")
@RequiredArgsConstructor
public class SummaryNewsController {

    private final SummaryNewsCacheRepository summaryNewsCacheRepository;
    private final UserRepository userRepository;
    private final UserInterestRepository userInterestRepository;

    /**
     * 사용자 맞춤 요약 뉴스 조회 (페이징)
     * - 사용자의 3개 관심사별로 4개씩 = 총 12개
     * - page 0-3: 각 페이지당 3개 (각 카테고리 1개씩)
     */
    @GetMapping
    public ResponseEntity<List<SummaryNewsDto>> getPersonalizedSummaryNews(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(value = "page", defaultValue = "0") int page) {

        String username = userDetails.getUsername();
        log.info("요약 뉴스 조회 요청 - 사용자: {}, 페이지: {}", username, page);

        try {
            // 1. 사용자 조회
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다: " + username));

            // 2. 사용자 관심사 조회 (정확히 3개여야 함)
            List<UserInterest> userInterests = userInterestRepository.findByUser(user);

            if (userInterests.size() != 3) {
                throw new CustomException(HttpStatus.BAD_REQUEST,
                        "관심사가 3개로 설정되지 않았습니다. 현재: " + userInterests.size() + "개");
            }

            // 3. 각 관심사별로 캐시된 요약 뉴스 4개씩 가져오기 (총 12개)
            // 최근 12시간 이내 생성된 캐시만 조회 (하루 3번 생성이므로 충분)
            LocalDateTime since = LocalDateTime.now().minusHours(12);
            List<List<SummaryNewsDto>> categoryNews = new ArrayList<>();

            for (UserInterest userInterest : userInterests) {
                Integer sectionId = userInterest.getInterest().getId().intValue();

                // DB 캐시에서 조회
                List<SummaryNewsCache> cachedNews = summaryNewsCacheRepository
                        .findBySectionIdAndGeneratedAtAfter(sectionId, since);

                if (cachedNews.isEmpty()) {
                    log.warn("섹션 {}에 대한 캐시된 요약 뉴스가 없습니다.", sectionId);
                    throw new CustomException(HttpStatus.SERVICE_UNAVAILABLE,
                            "요약 뉴스가 아직 준비되지 않았습니다. 잠시 후 다시 시도해주세요.");
                }

                // Cache -> DTO 변환
                List<SummaryNewsDto> newsForCategory = cachedNews.stream()
                        .map(cache -> SummaryNewsDto.builder()
                                .sectionId(cache.getSectionId())
                                .sectionName(cache.getSectionName())
                                .title(cache.getTitle())
                                .url(cache.getUrl())
                                .summary(cache.getSummary())
                                .build())
                        .collect(Collectors.toList());

                categoryNews.add(newsForCategory);
            }

            // 4. 페이징 처리: 각 카테고리에서 page번째 뉴스를 가져옴
            // page 0: 각 카테고리의 0번째 = 3개
            // page 1: 각 카테고리의 1번째 = 3개
            // page 2: 각 카테고리의 2번째 = 3개
            // page 3: 각 카테고리의 3번째 = 3개

            if (page < 0 || page >= 4) {
                throw new CustomException(HttpStatus.BAD_REQUEST, "페이지는 0~3 사이여야 합니다.");
            }

            List<SummaryNewsDto> result = new ArrayList<>();

            for (List<SummaryNewsDto> newsList : categoryNews) {
                if (page < newsList.size()) {
                    result.add(newsList.get(page));
                }
            }

            log.info("요약 뉴스 반환 - 사용자: {}, 페이지: {}, 개수: {}", username, page, result.size());
            return ResponseEntity.ok(result);

        } catch (CustomException e) {
            log.error("요약 뉴스 조회 실패 - 사용자: {}, 오류: {}", username, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("요약 뉴스 조회 중 예외 발생 - 사용자: {}, 오류: {}", username, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
