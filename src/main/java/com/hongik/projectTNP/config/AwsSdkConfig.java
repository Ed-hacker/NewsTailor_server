package com.hongik.projectTNP.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

@Configuration
public class AwsSdkConfig {

    @Value("${cloud.aws.credentials.access-key:YOUR_DUMMY_ACCESS_KEY}")
    private String accessKey;

    @Value("${cloud.aws.credentials.secret-key:YOUR_DUMMY_SECRET_KEY}")
    private String secretKey;

    @Value("${cloud.aws.region.static:ap-northeast-2}")
    private String region;

    @Bean
    public S3Client s3Client() {
        AwsBasicCredentials credentials = AwsBasicCredentials.create(accessKey, secretKey);
        return S3Client.builder()
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(credentials))
                .build();
    }

} 