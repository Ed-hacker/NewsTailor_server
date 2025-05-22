# 뉴스 요약 및 TTS 서비스

AI를 활용한 뉴스 기사 요약 및 음성 변환(TTS) 서비스입니다.

## 주요 기능

- 뉴스 기사 크롤링 및 저장
- OpenAI API를 활용한 뉴스 기사 요약
- Amazon Polly를 활용한 텍스트 음성 변환(TTS)
- 사용자 인증 및 권한 관리
- 커스텀 텍스트 요약 및 TTS 변환

## 기술 스택

- **백엔드**: Spring Boot, JPA, Spring Security
- **데이터베이스**: MySQL
- **인프라**: Docker, AWS (S3, Polly)
- **외부 API**: OpenAI API, News API
- **아키텍처**: MCP (Message-Context-Protocol) 패턴 적용

## 개발 환경 설정

### 필수 요구사항

- Java 17+
- Docker
- Docker Compose
- AWS 계정 (S3, Polly 사용)
- OpenAI API 키

### 환경 변수 설정

`.env` 파일을 프로젝트 루트에 생성하고 다음 변수들을 설정합니다:

```
# OpenAI API 설정
OPENAI_API_KEY=your_openai_api_key

# AWS 설정
AWS_ACCESS_KEY=your_aws_access_key
AWS_SECRET_KEY=your_aws_secret_key
AWS_S3_BUCKET=your_s3_bucket_name

# News API 설정
NEWS_API_KEY=your_news_api_key
```

### 실행 방법

```bash
# Docker Compose로 실행
docker-compose up -d

# 로컬에서 개발 환경으로 실행
./gradlew bootRun
```

## 아키텍처

```
[Client (React)] 
     ↓ HTTPS
[NGINX Container]
     ├─ /api → Spring API (news-backend)
     └─ /     → React (static build)

[Spring Boot Container]
 ├── AuthController
 ├── NewsController
 ├── AgentRunner (MCP 아키텍처)
 ├── Scheduler
 └── Service Layer
        ├─ SummaryService (MCP agent 확장 가능)
        └─ TtsService (MCP agent 확장 가능)

[MySQL Container] ← JPA Repository 연동
[Amazon S3] ← 음성 파일 저장
[External APIs]
 ├─ OpenAI
 └─ Amazon Polly
```

## MCP 아키텍처

본 프로젝트는 MCP(Message-Context-Protocol) 패턴을 적용하여 확장성을 높였습니다. 주요 구성요소:

- `AgentRunner`: 에이전트 실행 흐름 관리
- `Context`: 에이전트 실행에 필요한 데이터 컨텍스트
- `Service`: 실제 비즈니스 로직 구현 (SummaryService, TtsService 등)

## API 엔드포인트

### 인증 API

- `POST /api/auth/signup`: 회원가입
- `POST /api/auth/login`: 로그인

### 뉴스 API

- `GET /api/news`: 모든 뉴스 조회
- `GET /api/news/{id}`: 특정 뉴스 조회
- `POST /api/news/summarize/{id}`: 뉴스 요약 및 TTS 생성
- `POST /api/news/custom`: 커스텀 텍스트 요약 및 TTS 생성

## 라이센스

MIT 