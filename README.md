# 개인 맞춤형 뉴스 요약 및 TTS 서비스

사용자의 관심사에 기반한 뉴스 기사를 추천하고, 해당 기사를 요약하며, 요약된 내용을 음성으로 변환(TTS)하여 제공하는 서비스의 백엔드 애플리케이션입니다.

## 주요 기능

- 사용자 회원가입 및 로그인 (JWT 기반 인증)
- 사용자 관심사 선택 및 저장
- 선택된 관심사에 따른 개인화 뉴스 목록 제공 (페이징 지원)
- 뉴스 기사 상세 조회
- 뉴스 기사 좋아요 및 북마크 기능
- News API를 활용한 주기적인 뉴스 크롤링 및 저장 (Jsoup 연동 예정)
- OpenAI API를 활용한 뉴스 기사 요약 (API 키 필요)
- Amazon Polly 및 S3를 활용한 요약 텍스트 음성 변환(TTS) 및 오디오 파일 제공 (현재 API 키 문제로 임시 비활성화)

## 기술 스택

- **백엔드**: Spring Boot 3.x, Spring Data JPA, Spring Security 6.x
- **언어**: Java 17
- **빌드 도구**: Gradle
- **데이터베이스**: MySQL 8.x (로컬 환경) / H2 (테스트용 프로필 지원 가능)
- **인증**: JWT (JSON Web Token)
- **외부 API**:
    - NewsAPI (뉴스 수집용)
    - OpenAI API (기사 요약용)
    - AWS Polly (TTS 변환용, 현재 임시 비활성화)
    - AWS S3 (오디오 파일 저장용, 현재 임시 비활성화)
- **기타 라이브러리**: Lombok, Jsoup (예정), AWS SDK v2 for Java

**참고:** AWS 관련 기능(Polly TTS, S3 업로드)은 현재 코드 상에서 임시 비활성화되어 있어, `cloud.aws` 관련 키들이 없어도 애플리케이션의 기본 기능(회원가입, 로그인, 뉴스 조회 등)은 실행 및 테스트 가능합니다.


## API 엔드포인트 (Postman Collection 제공)

Postman을 사용하여 API를 테스트할 수 있습니다. 관련 Postman Collection JSON은 요청 시 제공 가능합니다.

### 인증 (`/api`)
- `POST /signup`: 회원가입
  - Request Body: `SignupRequestDto` (`email`, `password`, `nickname`)
- `POST /login`: 로그인
  - Request Body: `LoginRequestDto` (`email`, `password`)
  - Response Body: `TokenResponseDto` (`accessToken`, `refreshToken`)

### 관심사 (`/api`)
- `GET /interests`: 등록 가능한 모든 관심사 목록 조회 (인증 선택)
- `POST /user/interests`: 현재 로그인한 사용자의 관심사 등록/수정 (인증 필요)
  - Request Body: `UserInterestRequestDto` (`interestIds`: `List<Long>`)

### 뉴스 (`/api/news`)
모든 뉴스 관련 API는 인증(JWT 토큰)이 필요합니다.

- `GET /`: 개인화된 뉴스 목록 페이징 조회 (사용자 관심사 기반)
  - Query Parameters: `page` (0부터 시작), `size`
- `GET /{id}`: 특정 뉴스 상세 정보 조회
- `POST /{id}/like`: 특정 뉴스 '좋아요' 추가
- `DELETE /{id}/like`: 특정 뉴스 '좋아요' 취소
- `POST /{id}/bookmark`: 특정 뉴스 북마크 추가
- `DELETE /{id}/bookmark`: 특정 뉴스 북마크 취소
- `GET /{id}/audio`: 특정 뉴스의 TTS 오디오 정보 조회 (현재 TTS 기능 비활성화로 임시 메시지 반환)

## 아키텍처 (간략)

- **Controller Layer**: API 요청 수신 및 응답 처리
- **Service Layer**: 비즈니스 로직 수행
- **Repository Layer**: 데이터베이스 연동 (Spring Data JPA)
- **Domain Layer**: 핵심 엔티티 정의
- **Util Layer**: JWT 처리, S3 업로더 (현재 비활성화) 등 유틸리티 클래스
- **Config Layer**: Spring Security, AWS SDK (현재 비활성화), 기타 애플리케이션 설정
- **Scheduler Layer**: 주기적인 뉴스 크롤링 수행



## 향후 개선 사항
- Jsoup을 이용한 뉴스 기사 본문 크롤링 기능 완성
- OpenAI, AWS Polly/S3 연동 활성화 및 실제 API 키를 사용한 기능 구현
- 테스트 코드 작성 (단위 테스트, 통합 테스트)
- Dockerfile 및 docker-compose.yml을 통한 배포 환경 구성
- 프론트엔드 애플리케이션 연동
