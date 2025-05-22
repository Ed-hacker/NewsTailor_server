package com.hongik.projectTNP.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.UUID;

/**
 * Amazon S3에 파일을 업로드하기 위한 유틸리티 클래스
 */
@Component
public class S3Uploader {

    private final S3Client s3Client;
    
    @Value("${aws.s3.bucket}")
    private String bucket;
    
    @Value("${aws.s3.base-url}")
    private String baseUrl;

    @Autowired
    public S3Uploader(@Value("${aws.access.key}") String accessKey,
                      @Value("${aws.secret.key}") String secretKey,
                      @Value("${aws.region:ap-northeast-2}") String region) {
        
        AwsBasicCredentials awsCredentials = AwsBasicCredentials.create(accessKey, secretKey);
        
        this.s3Client = S3Client.builder()
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(awsCredentials))
                .build();
    }

    /**
     * 파일을 S3에 업로드합니다.
     *
     * @param file 업로드할 파일
     * @param dirName S3 디렉토리 경로
     * @return 업로드된 파일의 접근 URL
     * @throws RuntimeException 업로드 실패 시 발생
     */
    public String upload(File file, String dirName) {
        String fileName = dirName + "/" + UUID.randomUUID() + "_" + file.getName();
        
        try {
            byte[] fileContent = Files.readAllBytes(file.toPath());
            
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(fileName)
                    .contentType(determineContentType(file.getName()))
                    .build();
            
            PutObjectResponse response = s3Client.putObject(putObjectRequest, RequestBody.fromBytes(fileContent));
            
            if (response != null) {
                return baseUrl + "/" + fileName;
            } else {
                throw new RuntimeException("S3 업로드 응답이 null입니다.");
            }
        } catch (IOException e) {
            throw new RuntimeException("파일 업로드 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
    
    /**
     * 파일 확장자에 따른 Content-Type을 결정합니다.
     *
     * @param fileName 파일명
     * @return Content-Type
     */
    private String determineContentType(String fileName) {
        if (fileName.endsWith(".mp3")) {
            return "audio/mpeg";
        } else if (fileName.endsWith(".wav")) {
            return "audio/wav";
        } else if (fileName.endsWith(".jpg") || fileName.endsWith(".jpeg")) {
            return "image/jpeg";
        } else if (fileName.endsWith(".png")) {
            return "image/png";
        } else {
            return "application/octet-stream";
        }
    }
} 