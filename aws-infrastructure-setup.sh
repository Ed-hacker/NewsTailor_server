#!/bin/bash

# AWS 인프라 자동 설정 스크립트
# EC2 + RDS 환경 구성

set -e

echo "=== AWS 인프라 설정 시작 ==="

# 색상 정의
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

# 기본 설정
REGION="ap-northeast-2"
PROJECT_NAME="projecttnp"

# VPC 정보 가져오기
echo -e "${YELLOW}1. VPC 정보 조회 중...${NC}"
DEFAULT_VPC=$(aws ec2 describe-vpcs --filters "Name=is-default,Values=true" --query 'Vpcs[0].VpcId' --output text --region $REGION)
if [ "$DEFAULT_VPC" = "None" ]; then
    echo -e "${RED}기본 VPC를 찾을 수 없습니다. VPC를 먼저 생성해주세요.${NC}"
    exit 1
fi
echo "기본 VPC: $DEFAULT_VPC"

# 서브넷 정보 가져오기
SUBNETS=$(aws ec2 describe-subnets --filters "Name=vpc-id,Values=$DEFAULT_VPC" --query 'Subnets[].SubnetId' --output text --region $REGION)
SUBNET_ARRAY=($SUBNETS)

if [ ${#SUBNET_ARRAY[@]} -lt 2 ]; then
    echo -e "${RED}최소 2개의 서브넷이 필요합니다. (현재: ${#SUBNET_ARRAY[@]}개)${NC}"
    exit 1
fi

echo "사용할 서브넷: ${SUBNET_ARRAY[0]} ${SUBNET_ARRAY[1]}"

# EC2 보안 그룹 생성
echo -e "${YELLOW}2. EC2 보안 그룹 생성...${NC}"
EC2_SG_ID=$(aws ec2 create-security-group \
    --group-name ${PROJECT_NAME}-ec2-sg \
    --description "Security group for Project TNP EC2" \
    --vpc-id $DEFAULT_VPC \
    --query 'GroupId' \
    --output text \
    --region $REGION 2>/dev/null || \
    aws ec2 describe-security-groups \
    --filters "Name=group-name,Values=${PROJECT_NAME}-ec2-sg" "Name=vpc-id,Values=$DEFAULT_VPC" \
    --query 'SecurityGroups[0].GroupId' \
    --output text \
    --region $REGION)

echo "EC2 보안 그룹: $EC2_SG_ID"

# EC2 보안 그룹 규칙 추가
echo -e "${YELLOW}3. EC2 보안 그룹 규칙 설정...${NC}"
# SSH 허용
aws ec2 authorize-security-group-ingress \
    --group-id $EC2_SG_ID \
    --protocol tcp \
    --port 22 \
    --cidr 0.0.0.0/0 \
    --region $REGION 2>/dev/null || echo "SSH 규칙 이미 존재"

# HTTP (8080) 허용
aws ec2 authorize-security-group-ingress \
    --group-id $EC2_SG_ID \
    --protocol tcp \
    --port 8080 \
    --cidr 0.0.0.0/0 \
    --region $REGION 2>/dev/null || echo "HTTP(8080) 규칙 이미 존재"

# RDS 보안 그룹 생성
echo -e "${YELLOW}4. RDS 보안 그룹 생성...${NC}"
RDS_SG_ID=$(aws ec2 create-security-group \
    --group-name ${PROJECT_NAME}-rds-sg \
    --description "Security group for Project TNP RDS" \
    --vpc-id $DEFAULT_VPC \
    --query 'GroupId' \
    --output text \
    --region $REGION 2>/dev/null || \
    aws ec2 describe-security-groups \
    --filters "Name=group-name,Values=${PROJECT_NAME}-rds-sg" "Name=vpc-id,Values=$DEFAULT_VPC" \
    --query 'SecurityGroups[0].GroupId' \
    --output text \
    --region $REGION)

echo "RDS 보안 그룹: $RDS_SG_ID"

# RDS 보안 그룹 규칙 추가 (EC2에서만 접근 허용)
echo -e "${YELLOW}5. RDS 보안 그룹 규칙 설정...${NC}"
aws ec2 authorize-security-group-ingress \
    --group-id $RDS_SG_ID \
    --protocol tcp \
    --port 3306 \
    --source-group $EC2_SG_ID \
    --region $REGION 2>/dev/null || echo "MySQL 규칙 이미 존재"

# DB 서브넷 그룹 생성
echo -e "${YELLOW}6. DB 서브넷 그룹 생성...${NC}"
aws rds create-db-subnet-group \
    --db-subnet-group-name ${PROJECT_NAME}-subnet-group \
    --db-subnet-group-description "Subnet group for Project TNP" \
    --subnet-ids ${SUBNET_ARRAY[0]} ${SUBNET_ARRAY[1]} \
    --region $REGION 2>/dev/null || echo "서브넷 그룹 이미 존재"

echo -e "${GREEN}=== 인프라 설정 완료! ===${NC}"
echo ""
echo -e "${YELLOW}다음 정보를 기록해두세요:${NC}"
echo "EC2 보안 그룹 ID: $EC2_SG_ID"
echo "RDS 보안 그룹 ID: $RDS_SG_ID"
echo "DB 서브넷 그룹: ${PROJECT_NAME}-subnet-group"
echo ""
echo -e "${YELLOW}다음 단계:${NC}"
echo "1. EC2 인스턴스 생성시 보안 그룹: $EC2_SG_ID 사용"
echo "2. RDS 인스턴스 생성시 보안 그룹: $RDS_SG_ID, 서브넷 그룹: ${PROJECT_NAME}-subnet-group 사용"
echo ""

# RDS 생성 명령어 예시 출력
echo -e "${YELLOW}RDS 생성 명령어 예시:${NC}"
cat << EOF
aws rds create-db-instance \\
    --db-instance-identifier ${PROJECT_NAME}-db \\
    --db-instance-class db.t3.micro \\
    --engine mysql \\
    --engine-version 8.0 \\
    --master-username admin \\
    --master-user-password YOUR_SECURE_PASSWORD \\
    --allocated-storage 20 \\
    --storage-type gp3 \\
    --vpc-security-group-ids $RDS_SG_ID \\
    --db-subnet-group-name ${PROJECT_NAME}-subnet-group \\
    --backup-retention-period 7 \\
    --multi-az \\
    --storage-encrypted \\
    --region $REGION
EOF

echo ""
echo -e "${GREEN}설정 완료!${NC}"