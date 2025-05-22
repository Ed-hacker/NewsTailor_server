package com.hongik.projectTNP.service;

import com.hongik.projectTNP.util.S3Uploader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.polly.PollyClient;
import software.amazon.awssdk.services.polly.model.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.UUID;

@Service
public class PollyTtsService implements TtsService {

    private final PollyClient pollyClient;
    private final S3Uploader s3Uploader;
    
    @Value("${aws.s3.temp-dir:/tmp}")
    private String tempDir;

    @Autowired
    public PollyTtsService(S3Uploader s3Uploader, 
                          @Value("${aws.access.key}") String accessKey,
                          @Value("${aws.secret.key}") String secretKey,
                          @Value("${aws.region:ap-northeast-2}") String region) {
        this.s3Uploader = s3Uploader;
        
        AwsBasicCredentials awsCredentials = AwsBasicCredentials.create(accessKey, secretKey);
        
        this.pollyClient = PollyClient.builder()
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(awsCredentials))
                .build();
    }

    @Override
    public String generateAudio(String text) {
        try {
            String uuid = UUID.randomUUID().toString();
            String fileName = uuid + ".mp3";
            File outputFile = new File(tempDir, fileName);
            
            // Amazon Polly TTS 요청 생성
            SynthesizeSpeechRequest request = SynthesizeSpeechRequest.builder()
                    .outputFormat(OutputFormat.MP3)
                    .text(text)
                    .voiceId(VoiceId.SEOYEON) // 한국어 음성
                    .engine(Engine.NEURAL)
                    .build();
            
            // TTS 실행
            ResponseInputStream<SynthesizeSpeechResponse> responseStream = 
                    pollyClient.synthesizeSpeech(request);
            
            // 결과를 파일로 저장
            try (OutputStream outputStream = new FileOutputStream(outputFile)) {
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = responseStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
            }
            
            // S3에 업로드하고 URL 반환
            String s3Url = s3Uploader.upload(outputFile, "tts");
            
            // 임시 파일 삭제
            outputFile.delete();
            
            return s3Url;
            
        } catch (IOException | PollyException e) {
            e.printStackTrace();
            return null;
        }
    }
} 