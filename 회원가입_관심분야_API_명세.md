# 회원가입 관심분야 저장 방식 및 API 명세

## 데이터베이스 구조

### 1. `interest` 테이블 (관심분야 마스터)
```sql
CREATE TABLE interest (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE  -- 관심분야명 (예: "정치", "기술", "스포츠")
);
```

### 2. `user_interest` 테이블 (사용자-관심분야 연결)
```sql
CREATE TABLE user_interest (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,           -- 사용자 ID (외래키)
    interest_id BIGINT NOT NULL,       -- 관심분야 ID (외래키)
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (interest_id) REFERENCES interest(id)
);
```

## API 명세

### 1. 관심분야 목록 조회
```http
GET /api/interests
```

**응답 예시:**
```json
[
    {"id": 1, "name": "정치"},
    {"id": 2, "name": "경제"},
    {"id": 3, "name": "기술"},
    {"id": 4, "name": "스포츠"},
    {"id": 5, "name": "문화"}
]
```

### 2. 회원가입 (관심분야 포함)
```http
POST /api/auth/signup
Content-Type: application/json
```

**요청 본문:**
```json
{
    "username": "testuser",
    "password": "Test123!@#",
    "nickname": "테스트유저",
    "interestIds": [1, 3, 5]  // 선택한 관심분야 ID 배열
}
```

**응답:**
- 성공: `200 OK`
- 실패: `400 Bad Request` (아이디 중복, 잘못된 관심분야 ID 등)

## 프론트엔드 구현 가이드

### 1. 회원가입 페이지 로드 시
```javascript
// 관심분야 목록 가져오기
const getInterests = async () => {
    const response = await fetch('/api/interests');
    const interests = await response.json();
    return interests;
};
```

### 2. 관심분야 선택 UI
```javascript
// 관심분야 선택 상태 관리
const [selectedInterests, setSelectedInterests] = useState([]);

// 관심분야 선택/해제
const handleInterestToggle = (interestId) => {
    setSelectedInterests(prev => 
        prev.includes(interestId) 
            ? prev.filter(id => id !== interestId)
            : [...prev, interestId]
    );
};
```

### 3. 회원가입 요청
```javascript
const signup = async (userData) => {
    const signupData = {
        username: userData.username,
        password: userData.password,
        nickname: userData.nickname,
        interestIds: selectedInterests  // 선택된 관심분야 ID 배열
    };
    
    const response = await fetch('/api/auth/signup', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(signupData)
    });
    
    return response;
};
```

## 데이터 저장 과정

1. **사용자 생성**: `users` 테이블에 기본 정보 저장
2. **관심분야 연결**: 선택된 각 관심분야 ID에 대해 `user_interest` 테이블에 레코드 생성

### 예시
사용자가 "정치"(id:1), "기술"(id:3), "문화"(id:5)를 선택한 경우:

**users 테이블:**
```
id | username | password | nickname
1  | testuser | [암호화됨] | 테스트유저
```

**user_interest 테이블:**
```
id | user_id | interest_id
1  |    1    |     1      (정치)
2  |    1    |     3      (기술)
3  |    1    |     5      (문화)
```

## 주의사항

- `interestIds`는 선택사항입니다 (null 또는 빈 배열 가능)
- 존재하지 않는 관심분야 ID 전송 시 `400 Bad Request` 에러 발생
- 중복된 관심분야 ID는 자동으로 중복 제거됨
- 관심분야는 회원가입 후에도 `POST /api/user/interests`로 변경 가능