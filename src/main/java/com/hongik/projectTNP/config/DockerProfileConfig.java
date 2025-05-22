package com.hongik.projectTNP.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.beans.factory.annotation.Value;

@Configuration
public class DockerProfileConfig {

    @Value("${spring.datasource.url}")
    private String datasourceUrl;

    @Value("${openai.api.key:}")
    private String openaiApiKey;

    @Value("${aws.access.key:}")
    private String awsAccessKey;

    @Value("${aws.secret.key:}")
    private String awsSecretKey;

    @Bean
    @Profile("docker")
    public String logDockerProfileActivated() {
        System.out.println("Docker 프로필이 활성화되었습니다.");
        System.out.println("데이터베이스 URL: " + datasourceUrl);
        System.out.println("외부 API 연결 구성이 로드되었습니다.");
        return "Docker Profile Activated";
    }

    @Bean
    @Profile("local")
    public String logLocalProfileActivated() {
        System.out.println("로컬 개발 프로필이 활성화되었습니다.");
        System.out.println("데이터베이스 URL: " + datasourceUrl);
        return "Local Profile Activated";
    }
} 