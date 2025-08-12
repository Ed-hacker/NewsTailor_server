#!/bin/bash

# EC2 배포 스크립트
# 사용법: ./deploy.sh

set -e

echo "=== Project TNP EC2 배포 시작 ==="

# 색상 정의
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# 환경변수 파일 체크
if [ ! -f ".env.prod" ]; then
    echo -e "${RED}오류: .env.prod 파일이 없습니다.${NC}"
    echo "다음 내용으로 .env.prod 파일을 생성해주세요:"
    echo "DB_HOST=your-rds-endpoint"
    echo "DB_NAME=projecttnp"
    echo "DB_USERNAME=admin"
    echo "DB_PASSWORD=your-password"
    echo "JWT_SECRET=your-jwt-secret"
    exit 1
fi

# 환경변수 로드
source .env.prod

echo -e "${YELLOW}1. 프로젝트 빌드 중...${NC}"
./gradlew clean build -x test

if [ $? -ne 0 ]; then
    echo -e "${RED}빌드 실패!${NC}"
    exit 1
fi

echo -e "${GREEN}빌드 완료!${NC}"

echo -e "${YELLOW}2. 기존 프로세스 종료 중...${NC}"
# Spring Boot 애플리케이션 프로세스 찾기 및 종료
PID=$(pgrep -f "java.*projectTNP" || true)
if [ ! -z "$PID" ]; then
    echo "기존 프로세스 (PID: $PID) 종료 중..."
    kill -15 $PID
    sleep 5
    
    # SIGTERM으로 종료되지 않으면 SIGKILL 사용
    if kill -0 $PID 2>/dev/null; then
        echo "강제 종료..."
        kill -9 $PID
    fi
    echo -e "${GREEN}기존 프로세스 종료 완료${NC}"
else
    echo "실행 중인 프로세스가 없습니다."
fi

echo -e "${YELLOW}3. 로그 디렉토리 생성...${NC}"
sudo mkdir -p /var/log/projecttnp
sudo chown $USER:$USER /var/log/projecttnp

echo -e "${YELLOW}4. 애플리케이션 시작...${NC}"
# JAR 파일 찾기
JAR_FILE=$(find build/libs -name "*.jar" -not -name "*-plain.jar" | head -1)

if [ -z "$JAR_FILE" ]; then
    echo -e "${RED}JAR 파일을 찾을 수 없습니다!${NC}"
    exit 1
fi

echo "JAR 파일: $JAR_FILE"

# 백그라운드에서 애플리케이션 실행
nohup java -jar \
    -Dspring.profiles.active=prod \
    -Xms512m -Xmx1024m \
    -DDB_HOST="$DB_HOST" \
    -DDB_NAME="$DB_NAME" \
    -DDB_USERNAME="$DB_USERNAME" \
    -DDB_PASSWORD="$DB_PASSWORD" \
    -DJWT_SECRET="$JWT_SECRET" \
    "$JAR_FILE" > /var/log/projecttnp/nohup.out 2>&1 &

echo "새 프로세스 PID: $!"

echo -e "${YELLOW}5. 애플리케이션 상태 확인 중...${NC}"
sleep 10

# Health Check
for i in {1..10}; do
    if curl -s http://localhost:8080/api/actuator/health >/dev/null 2>&1; then
        echo -e "${GREEN}✓ 애플리케이션이 성공적으로 시작되었습니다!${NC}"
        echo -e "${GREEN}Health Check: http://localhost:8080/api/actuator/health${NC}"
        break
    else
        echo "대기 중... ($i/10)"
        sleep 5
    fi
    
    if [ $i -eq 10 ]; then
        echo -e "${RED}애플리케이션 시작 실패. 로그를 확인해주세요:${NC}"
        echo "tail -f /var/log/projecttnp/nohup.out"
        exit 1
    fi
done

echo -e "${GREEN}=== 배포 완료! ===${NC}"
echo "로그 확인: tail -f /var/log/projecttnp/application.log"
echo "nohup 로그: tail -f /var/log/projecttnp/nohup.out"
echo "프로세스 상태: ps aux | grep java"