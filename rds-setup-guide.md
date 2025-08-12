# RDS MySQL 설정 가이드

## 1. RDS 인스턴스 생성

### 기본 설정
- **엔진 타입**: MySQL 8.0
- **인스턴스 클래스**: db.t3.micro (프리티어) 또는 db.t3.small
- **할당된 스토리지**: 20GB (gp3)
- **다중 AZ**: 비활성화 (개발용) / 활성화 (프로덕션)

### 인증 정보
- **마스터 사용자명**: `admin`
- **마스터 암호**: 강력한 암호 설정 (최소 8자, 특수문자 포함)

### 네트워크 설정
- **VPC**: 기본 VPC 또는 사용자 정의 VPC
- **서브넷 그룹**: 생성 필요 (아래 참조)
- **퍼블릭 액세스**: 아니요 (보안상 권장)
- **보안 그룹**: 생성 필요 (아래 참조)

## 2. DB 서브넷 그룹 생성

```bash
# AWS CLI로 서브넷 그룹 생성
aws rds create-db-subnet-group \
    --db-subnet-group-name projecttnp-subnet-group \
    --db-subnet-group-description "Subnet group for Project TNP" \
    --subnet-ids subnet-xxxxxxxx subnet-yyyyyyyy \
    --region ap-northeast-2
```

## 3. 보안 그룹 생성

### RDS 보안 그룹
```bash
# RDS 보안 그룹 생성
aws ec2 create-security-group \
    --group-name projecttnp-rds-sg \
    --description "Security group for Project TNP RDS" \
    --region ap-northeast-2

# MySQL 포트(3306) 허용 - EC2에서만 접근
aws ec2 authorize-security-group-ingress \
    --group-name projecttnp-rds-sg \
    --protocol tcp \
    --port 3306 \
    --source-group projecttnp-ec2-sg \
    --region ap-northeast-2
```

### EC2 보안 그룹 (기존 업데이트)
```bash
# EC2 보안 그룹에 아웃바운드 MySQL 규칙 추가 (필요시)
aws ec2 authorize-security-group-egress \
    --group-name projecttnp-ec2-sg \
    --protocol tcp \
    --port 3306 \
    --destination-group projecttnp-rds-sg \
    --region ap-northeast-2
```

## 4. 데이터베이스 및 사용자 설정

RDS 인스턴스가 생성되면 다음 SQL을 실행:

```sql
-- 데이터베이스 생성
CREATE DATABASE projecttnp CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 애플리케이션용 사용자 생성 (선택사항)
CREATE USER 'projecttnp_user'@'%' IDENTIFIED BY 'secure_password';
GRANT ALL PRIVILEGES ON projecttnp.* TO 'projecttnp_user'@'%';
FLUSH PRIVILEGES;
```

## 5. 환경변수 설정

`.env.prod` 파일 업데이트:
```bash
# RDS 엔드포인트 (콘솔에서 확인)
DB_HOST=projecttnp-db.xxxxxxxxx.ap-northeast-2.rds.amazonaws.com
DB_PORT=3306
DB_NAME=projecttnp
DB_USERNAME=admin
# 또는 DB_USERNAME=projecttnp_user (사용자 생성시)
DB_PASSWORD=your-rds-master-password
```

## 6. 연결 테스트

### EC2에서 RDS 연결 확인
```bash
# MySQL 클라이언트 설치 (Amazon Linux 2)
sudo yum install -y mysql

# 연결 테스트
mysql -h your-rds-endpoint.rds.amazonaws.com -u admin -p
```

### 애플리케이션에서 연결 확인
```bash
# Health check 엔드포인트로 확인
curl http://localhost:8080/api/actuator/health
```

## 7. 보안 권장사항

1. **암호 정책**: 복잡한 암호 사용
2. **SSL 연결**: `useSSL=true` 설정 유지
3. **방화벽**: RDS는 EC2에서만 접근 허용
4. **백업**: 자동 백업 활성화
5. **모니터링**: CloudWatch 로그 활성화

## 8. 비용 최적화

- **프리티어**: db.t3.micro, 20GB 스토리지 무료
- **개발환경**: 사용하지 않을 때 인스턴스 중지
- **스토리지**: gp2 대신 gp3 사용 (더 저렴)

## 트러블슈팅

### 연결 실패시 체크리스트
1. 보안 그룹 설정 확인
2. 서브넷 그룹 설정 확인  
3. RDS 상태 확인 (available)
4. 네트워크 ACL 확인
5. 방화벽 설정 확인