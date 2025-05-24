package com.hongik.projectTNP.util;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
// import org.springframework.stereotype.Component; // 주석 처리
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.GetUrlRequest;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.UUID;

/**
 * Amazon S3에 파일을 업로드하기 위한 유틸리티 클래스
 */
// @Component // 주석 처리
@RequiredArgsConstructor
public class S3Uploader {

    private static final Logger log = LoggerFactory.getLogger(S3Uploader.class);
    private final S3Client s3Client;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    /**
     * MultipartFile을 직접 S3에 업로드합니다.
     *
     * @param multipartFile 업로드할 MultipartFile
     * @param dirName S3 디렉토리 경로
     * @return 업로드된 파일의 접근 URL
     * @throws IOException 업로드 중 오류가 발생할 경우
     */
    public String upload(MultipartFile multipartFile, String dirName) throws IOException {
        if (multipartFile == null || multipartFile.isEmpty()) {
            return null;
        }
        String originalFileName = multipartFile.getOriginalFilename();
        String uniqueFileName = dirName + "/" + UUID.randomUUID().toString() + "_" + originalFileName;

        s3Client.putObject(PutObjectRequest.builder()
                        .bucket(bucket)
                        .key(uniqueFileName)
                        .build(),
                RequestBody.fromBytes(multipartFile.getBytes()));

        return s3Client.utilities().getUrl(GetUrlRequest.builder().bucket(bucket).key(uniqueFileName).build()).toExternalForm();
    }

    /**
     * 바이트 배열을 S3에 업로드합니다.
     *
     * @param fileBytes 업로드할 바이트 배열
     * @param dirName S3 디렉토리 경로
     * @param fileName 파일명
     * @param contentType 파일의 Content-Type
     * @return 업로드된 파일의 접근 URL
     * @throws IOException 업로드 중 오류가 발생할 경우
     */
    public String upload(byte[] fileBytes, String dirName, String fileName, String contentType) throws IOException {
        String uniqueFilename = dirName + "/" + UUID.randomUUID().toString() + "_" + fileName;

        // 임시 반환 (S3 연동 필요)
        return "https://" + bucket + ".s3.amazonaws.com/" + uniqueFilename;
    }

    // File 객체를 업로드하는 새로운 메소드
    public String upload(File file, String s3KeyName) throws IOException {
        if (file == null || !file.exists()) {
            log.error("업로드할 파일이 존재하지 않거나 null입니다: {}", file != null ? file.getName() : "null");
            return null;
        }
        // s3KeyName은 dirName/fileName.ext 형태의 전체 경로를 의미한다고 가정
        // 예: "audio/tts_123.mp3"
        
        s3Client.putObject(PutObjectRequest.builder()
                        .bucket(bucket)
                        .key(s3KeyName)
                        .build(),
                RequestBody.fromFile(Paths.get(file.toURI())));
        
        return s3Client.utilities().getUrl(GetUrlRequest.builder().bucket(bucket).key(s3KeyName).build()).toExternalForm();
    }
} 