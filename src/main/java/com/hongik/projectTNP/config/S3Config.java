package com.hongik.projectTNP.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

/**
 * AWS S3 설정 클래스
 * IAM Role 기반 인증을 사용합니다.
 * EC2 인스턴스에 연결된 IAM Role의 자격 증명을 자동으로 감지하고 사용합니다.
 *
 * 참고: 로컬 개발 환경에서는 ~/.aws/credentials 파일의 자격 증명을 사용합니다.
 */
@Configuration
public class S3Config {

    private final String awsRegion = "ap-northeast-2"; // 사용 중인 AWS 리전

    @Bean
    public S3Client s3Client() {
        // AWS SDK가 자동으로 자격 증명을 감지합니다:
        // 1. EC2 인스턴스 메타데이터 (IAM Role)
        // 2. 환경 변수 (AWS_ACCESS_KEY_ID, AWS_SECRET_ACCESS_KEY)
        // 3. ~/.aws/credentials 파일 (로컬 개발용)
        return S3Client.builder()
                .region(Region.of(awsRegion))
                .build();
    }

}