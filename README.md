# 개인 맞춤형 뉴스 요약 서비스 NewsTailor

사용자의 관심사에 기반한 뉴스를 제공하고, AI를 활용하여 뉴스를 요약하는 개인화된 뉴스 서비스의 백엔드 애플리케이션입니다.

## 주요 기능

### 핵심 기능
- **사용자 인증 시스템**: JWT 기반 회원가입 및 로그인
- **관심사 기반 개인화**: 사용자의 관심사를 선택하여 맞춤형 뉴스 제공
- **AI 뉴스 요약**: Google Gemini API를 활용한 뉴스 기사 자동 요약
- **뉴스 랭킹 시스템**: 하이브리드 알고리즘을 통한 상위 20개 뉴스 선정
  - Phase 1: 형태소 분석 기반 키워드 클러스터링
  - Phase 2: Gemini AI 기반 최종 선정
- **뉴스 클러스터링**: 한국어 형태소 분석기를 활용한 유사 뉴스 그룹핑
- **자동 카테고리 분류**: AI 기반 뉴스 카테고리 자동 태깅
- **북마크 기능**: 관심 뉴스 저장 및 관리 (사용자당 최대 50개)

### 뉴스 수집 및 처리
- **자동 크롤링**: 네이버 뉴스 사이트에서 실시간 뉴스 수집 (Jsoup)
- **스케줄러 기반 자동화**: 하루 3번 (08:00, 13:00, 20:00) 자동 실행
- **캐시 시스템**: 요약 뉴스 캐싱을 통한 API 비용 94% 절감
- **섹션별 분류**: 정치, 경제, 사회, 생활문화, IT과학, 세계 등 6개 섹션

## 기술 스택

### Backend
- **Framework**: Spring Boot 3.x
- **Language**: Java 17
- **Build Tool**: Gradle
- **ORM**: Spring Data JPA / Hibernate
- **Security**: Spring Security 6.x

### Database
- **Production**: Amazon RDS MySQL 8.x
- **File Storage**: Amazon S3

### AI & NLP
- **LLM**: Google Gemini API (뉴스 요약 및 카테고리 분류)
- **형태소 분석**: 한국어 형태소 분석기 (뉴스 클러스터링)

### Infrastructure
- **Cloud**: AWS
  - EC2 (애플리케이션 서버)
  - Application Load Balancer (로드 밸런싱 및 헬스체크)
  - RDS MySQL (데이터베이스)
  - S3 (파일 저장소)
- **Frontend Deployment**: Vercel (프론트엔드 분리 배포)

### 인증
- **JWT**: JSON Web Token 기반 인증

### 외부 라이브러리
- **Jsoup**: 웹 크롤링
- **Lombok**: 보일러플레이트 코드 감소
- **AWS SDK v2**: AWS 서비스 연동
- **RestTemplate**: HTTP 클라이언트

## 시스템 아키텍처

### 서버 구조
```
[Client (Vercel)]
        ↓
[AWS Application Load Balancer]
        ↓
[EC2 - Spring Boot Backend]
        ↓
[RDS MySQL] / [S3]
```

### 계층 구조
- **Controller Layer**: API 요청 수신 및 응답 처리
- **Service Layer**: 비즈니스 로직 수행
  - NewsRankingService: 랭킹 뉴스 관리
  - NewsSelectionService: 요약 뉴스 생성
  - NewsClusteringService: 뉴스 클러스터링
  - GeminiSummaryService: AI 요약 생성
  - BookmarkService: 북마크 관리
  - AuthService: 인증 처리
- **Repository Layer**: 데이터베이스 연동 (Spring Data JPA)
- **Domain Layer**: 핵심 엔티티 (User, NewsRanking, SummaryNewsCache, Bookmark, etc.)
- **Crawler Layer**: 뉴스 크롤링
  - NaverRankingCrawler: 랭킹 뉴스 크롤러
  - NaverSectionCrawler: 섹션별 뉴스 크롤러
  - ArticleContentCrawler: 기사 본문 크롤러
- **Scheduler Layer**: 주기적 작업 수행
  - SummaryNewsScheduler: 요약 뉴스 생성 및 캐싱

## API 엔드포인트

### 인증 (`/api`)
- `POST /signup`: 회원가입
  - Request Body: `SignupRequestDto` (`username`, `password`, `email`, `nickname`)
  - Response: `TokenResponseDto` (`accessToken`, `refreshToken`)
- `POST /login`: 로그인
  - Request Body: `LoginRequestDto` (`username`, `password`)
  - Response: `TokenResponseDto` (`accessToken`, `refreshToken`)
- `GET /username/check`: 사용자 이름 중복 확인
  - Query Parameter: `username`
  - Response: `UsernameCheckResponseDto` (`available`)

### 관심사 (`/api`)
- `GET /interests`: 등록 가능한 모든 관심사 목록 조회
  - Response: `List<InterestResponseDto>`
- `POST /user/interests`: 사용자 관심사 등록/수정 (인증 필요)
  - Request Body: `UserInterestRequestDto` (`interestIds`: `List<Long>`)

### 랭킹 뉴스 (`/api/v1/news/ranking`)
- `GET /`: 글로벌 Top 20 랭킹 뉴스 조회
  - Query Parameter: `limit` (기본값: 20)
  - Response: `List<NewsRankingResponse>`
- `GET /section/{sectionId}`: 특정 섹션의 랭킹 뉴스 조회
  - Path Variable: `sectionId` (100=정치, 101=경제, 102=사회, 103=생활문화, 105=IT과학, 104=세계)
  - Query Parameter: `date` (YYYY-MM-DD)

### 요약 뉴스 (`/api/v1/news/summary`)
- `GET /section/{sectionId}`: 특정 섹션의 요약 뉴스 조회 (캐시 조회)
  - Path Variable: `sectionId`
  - Response: `List<SummaryNewsDto>` (최대 4개)

### 북마크 (`/api/v1/bookmarks`)
인증 필요
- `GET /`: 사용자의 모든 북마크 조회
  - Response: `List<BookmarkResponseDto>`
- `POST /`: 북마크 추가
  - Request Body: `BookmarkRequestDto` (`title`, `url`, `summary`, `sectionId`)
- `DELETE /{id}`: 북마크 삭제
  - Path Variable: `id` (북마크 ID)

### 사용자 (`/api/v1/users`)
인증 필요
- `GET /me`: 현재 로그인한 사용자 정보 조회
  - Response: `UserInfoDto`
- `PUT /me`: 사용자 프로필 업데이트
  - Request Body: `ProfileUpdateDto` (`nickname`, `email`)
- `DELETE /me`: 회원 탈퇴

### 헬스체크 (`/health`)
- `GET /`: 서버 상태 확인 (ALB 헬스체크용)
  - Response: `{"status": "UP"}`

### 테스트 (`/api/test`)
개발 및 테스트용 엔드포인트
- `POST /summary`: 텍스트 요약 테스트
  - Request Body: `String` (요약할 텍스트)
- `GET /gemini`: Gemini 연결 테스트
  - Query Parameter: `sectionId` (기본값: 100)
- `POST /generate-all`: 전체 섹션 요약 뉴스 생성
- `POST /full-process`: 전체 프로세스 실행 (크롤링 → 요약 → 캐시)
- `POST /ranking-algorithm`: 하이브리드 랭킹 알고리즘 테스트

## 환경 설정

### 필수 환경 변수 (application.properties)
```properties
# Database
spring.datasource.url=jdbc:mysql://[RDS_ENDPOINT]:3306/[DB_NAME]
spring.datasource.username=[DB_USERNAME]
spring.datasource.password=[DB_PASSWORD]

# JWT
jwt.secret=[YOUR_JWT_SECRET]
jwt.expiration=[ACCESS_TOKEN_EXPIRATION_MS]
jwt.refresh-expiration=[REFRESH_TOKEN_EXPIRATION_MS]

# Gemini API
gemini.api.key=[YOUR_GEMINI_API_KEY]
gemini.api.url=https://generativelanguage.googleapis.com/v1/models/gemini-1.5-flash:generateContent

# AWS S3 (IAM Role 기반 자동 인증)
cloud.aws.region.static=[AWS_REGION]
cloud.aws.s3.bucket=[S3_BUCKET_NAME]
```

## 주요 알고리즘

### 1. 하이브리드 뉴스 랭킹 알고리즘
1. **Phase 1: 키워드 클러스터링**
   - 한국어 형태소 분석기를 통한 키워드 추출
   - TF-IDF 유사도 기반 클러스터링
   - 중복/유사 뉴스 그룹핑

2. **Phase 2: Gemini AI 최종 선정**
   - 각 클러스터에서 대표 뉴스 선정
   - 다양성 보장 (클러스터별 1개)
   - 상위 20개 뉴스 최종 선정

### 2. 캐시 기반 요약 시스템
- 스케줄러가 하루 3번 미리 요약 생성
- 데이터베이스 캐시 저장 (SummaryNewsCache)
- 사용자 요청 시 캐시에서 즉시 조회
- API 비용 94% 절감 효과

### 3. 형태소 분석 기반 클러스터링
- 한국어 불용어 제거
- 명사, 동사 등 핵심 단어 추출
- 코사인 유사도 계산
- 유사도 임계값 기반 클러스터링

## 데이터베이스 스키마

### 주요 테이블
- `user`: 사용자 정보
- `interest`: 관심사 목록
- `user_interest`: 사용자-관심사 매핑
- `news_ranking`: 랭킹 뉴스 (상위 20개)
- `summary_news_cache`: 요약 뉴스 캐시
- `bookmark`: 사용자 북마크

## 빌드 및 실행

### 로컬 실행
```bash
# 빌드
./gradlew build

# 실행
./gradlew bootRun

# 또는
java -jar build/libs/projectTNP-0.0.1-SNAPSHOT.jar
```

### 프로파일
- `local`: 로컬 개발 환경 (application-local.properties)
- `default`: 프로덕션 환경 (application.properties)

## 성과

### 비용 최적화
- Gemini API 비용 **94% 절감** (캐싱 시스템 도입)
- 사용자 요청마다 API 호출 → 하루 3번 스케줄러 실행

### 시스템 개선
- 실시간 생성 → 캐시 기반 시스템 전환
- 단일 EC2 → 프론트엔드(Vercel) + 백엔드(EC2/ALB) 분리
- NGINX → AWS ALB 로드 밸런서 교체
- 단일 장애점 제거 및 고가용성 확보

### 알고리즘 개발
- 뉴스 클러스터링 알고리즘 자체 개발
- 한국어 형태소 분석기 도입
- 하이브리드 랭킹 알고리즘 구현

## 향후 개선 사항

- [ ] 테스트 코드 작성 (단위 테스트, 통합 테스트)
- [ ] CI/CD 파이프라인 구축
- [ ] Auto Scaling Group 설정을 통한 자동 확장
- [ ] Redis를 활용한 캐싱 전략 개선
- [ ] 로깅 및 모니터링 시스템 구축 (CloudWatch, ELK Stack)
- [ ] API 문서화 자동화 (Swagger/OpenAPI)
- [ ] 성능 최적화 (쿼리 최적화, 인덱싱)
- [ ] 추천 알고리즘 고도화

## 라이선스

이 프로젝트는 개인 학습 및 포트폴리오 목적으로 개발되었습니다.

## 개발자

강현욱 (Kang Hyun-uk)
