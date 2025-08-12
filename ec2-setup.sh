#!/bin/bash

# EC2 인스턴스 초기 설정 스크립트
# Amazon Linux 2/Ubuntu 22.04 대응

echo "=== EC2 인스턴스 초기 설정 시작 ==="

# OS 감지
if [ -f /etc/os-release ]; then
    . /etc/os-release
    OS=$NAME
fi

echo "감지된 OS: $OS"

# 패키지 업데이트
echo "1. 패키지 업데이트 중..."
if [[ "$OS" == *"Amazon Linux"* ]]; then
    sudo yum update -y
elif [[ "$OS" == *"Ubuntu"* ]]; then
    sudo apt update && sudo apt upgrade -y
fi

# Java 21 설치
echo "2. Java 21 설치 중..."
if [[ "$OS" == *"Amazon Linux"* ]]; then
    # Amazon Corretto 21 설치
    sudo yum install -y java-21-amazon-corretto-devel
elif [[ "$OS" == *"Ubuntu"* ]]; then
    # OpenJDK 21 설치
    sudo apt install -y openjdk-21-jdk
fi

# JAVA_HOME 설정
echo "3. JAVA_HOME 설정..."
if [[ "$OS" == *"Amazon Linux"* ]]; then
    echo 'export JAVA_HOME=/usr/lib/jvm/java-21-amazon-corretto' >> ~/.bashrc
elif [[ "$OS" == *"Ubuntu"* ]]; then
    echo 'export JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64' >> ~/.bashrc
fi
echo 'export PATH=$JAVA_HOME/bin:$PATH' >> ~/.bashrc
source ~/.bashrc

# MySQL 클라이언트 설치 (RDS 연결용)
echo "4. MySQL 클라이언트 설치..."
if [[ "$OS" == *"Amazon Linux"* ]]; then
    sudo yum install -y mysql
elif [[ "$OS" == *"Ubuntu"* ]]; then
    sudo apt install -y mysql-client
fi
echo "MySQL 클라이언트 설치 완료. RDS 연결 테스트에 사용할 수 있습니다."

# Git 설치
echo "5. Git 설치..."
if [[ "$OS" == *"Amazon Linux"* ]]; then
    sudo yum install -y git
elif [[ "$OS" == *"Ubuntu"* ]]; then
    sudo apt install -y git
fi

# 방화벽 설정 (Ubuntu의 경우)
if [[ "$OS" == *"Ubuntu"* ]]; then
    echo "6. 방화벽 설정..."
    sudo ufw allow ssh
    sudo ufw allow 8080
    sudo ufw --force enable
fi

# AWS CLI v2 설치
echo "7. AWS CLI v2 설치..."
curl "https://awscli.amazonaws.com/awscli-exe-linux-x86_64.zip" -o "awscliv2.zip"
if command -v unzip &> /dev/null; then
    unzip awscliv2.zip
else
    if [[ "$OS" == *"Amazon Linux"* ]]; then
        sudo yum install -y unzip
    elif [[ "$OS" == *"Ubuntu"* ]]; then
        sudo apt install -y unzip
    fi
    unzip awscliv2.zip
fi
sudo ./aws/install
rm -rf awscliv2.zip aws/

# 사용자 디렉토리 생성
echo "8. 애플리케이션 디렉토리 생성..."
mkdir -p ~/apps/projecttnp
cd ~/apps/projecttnp

echo "=== EC2 설정 완료 ==="
echo ""
echo "다음 단계:"
echo "1. 프로젝트 클론: git clone <repository-url>"
echo "2. 환경변수 설정: cp .env.prod.example .env.prod && vi .env.prod"
echo "3. 배포 실행: chmod +x deploy.sh && ./deploy.sh"
echo ""
echo "Java 버전 확인:"
java -version
echo ""
echo "현재 디렉토리: $(pwd)"