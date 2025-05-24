package com.hongik.projectTNP.service.impl;

import com.hongik.projectTNP.domain.Summary;
import com.hongik.projectTNP.domain.Tts;
import com.hongik.projectTNP.repository.TtsRepository;
import com.hongik.projectTNP.service.TtsService;
import com.hongik.projectTNP.util.S3Uploader; // S3Uploader 주입
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import software.amazon.awssdk.core.ResponseInputStream; // 수정된 import
import software.amazon.awssdk.services.polly.PollyClient;
import software.amazon.awssdk.services.polly.model.Engine;
import software.amazon.awssdk.services.polly.model.OutputFormat;
import software.amazon.awssdk.services.polly.model.SynthesizeSpeechRequest;
import software.amazon.awssdk.services.polly.model.SynthesizeSpeechResponse;
// import software.amazon.awssdk.services.polly.model.ResponseInputStream; // 중복 또는 잘못된 위치
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException; // IOException 추가
import java.time.LocalDateTime; // 추가

// import software.amazon.awssdk.services.polly.PollyClient; // AWS SDK v2
// import software.amazon.awssdk.services.polly.model.*;
// import java.io.InputStream;
// import java.io.File;
// import java.io.FileOutputStream;
// import java.nio.file.Files;
// import java.nio.file.Path;

@Slf4j
// @Service // 주석 처리
@RequiredArgsConstructor
public class TtsServiceImpl implements TtsService {

    private final TtsRepository ttsRepository;
    private final S3Uploader s3Uploader;
    private final PollyClient pollyClient; // PollyClient 주입 (주석 해제)

    @Value("${aws.s3.tts.bucket:your-s3-tts-bucket}") // S3 버킷명
    private String ttsBucketName;
    @Value("${aws.polly.voiceId:Seoyeon}") // Polly Voice ID (예: Joanna, Seoyeon)
    private String pollyVoiceId;

    @Override
    @Transactional
    public Tts generateAndUploadTts(Summary summary) {
        if (summary == null || summary.getSummary_text() == null || summary.getSummary_text().isEmpty()) {
            log.warn("TTS 생성할 요약 텍스트가 없습니다: Summary ID {}", summary != null ? summary.getId() : "null");
            return null; // 또는 예외 처리
        }
        // 파일명에 확장자를 명시적으로 포함 (s3Uploader가 처리할 수도 있지만 명확히 하기 위함)
        String fileName = "tts_" + summary.getId() + "_" + System.currentTimeMillis() + ".mp3";
        String audioUrl = generateAudioWithPollyAndUploadToS3(summary.getSummary_text(), fileName);

        Tts tts = Tts.builder()
                .summary(summary)
                .audioUrl(audioUrl)
                .build();
        return ttsRepository.save(tts);
    }

    @Override
    public String generateAudio(String textToSynthesize) {
        if (textToSynthesize == null || textToSynthesize.isEmpty()) {
            return null;
        }
        String fileName = "temp_tts_" + System.currentTimeMillis() + ".mp3";
        return generateAudioWithPollyAndUploadToS3(textToSynthesize, fileName);
    }

    // 실제 AWS Polly API 호출 및 S3 업로드 로직 (프로토타입)
    private String generateAudioWithPollyAndUploadToS3(String text, String s3FileName) {
        log.info("AWS Polly API 호출하여 TTS 생성 및 S3 업로드 시도 (파일: {})...", s3FileName);
        String s3Directory = "audio/";
        File tempAudioFile = null;
        try {
            // 1. Polly로 오디오 스트림 생성
            SynthesizeSpeechRequest synthesizeSpeechRequest = SynthesizeSpeechRequest.builder()
                    .text(text)
                    .voiceId(pollyVoiceId)
                    .outputFormat(OutputFormat.MP3)
                    .engine(Engine.NEURAL) // 고품질 Neural 엔진 사용 (선택적)
                    .build();
            
            // synthesizeSpeech는 ResponseInputStream을 반환 (SDK v2)
            ResponseInputStream<SynthesizeSpeechResponse> audioStream = pollyClient.synthesizeSpeech(synthesizeSpeechRequest);

            // 2. 스트림을 임시 파일로 저장
            tempAudioFile = File.createTempFile("polly-tts-", ".mp3");
            try (FileOutputStream fos = new FileOutputStream(tempAudioFile)) {
                byte[] buffer = new byte[1024];
                int length;
                while ((length = audioStream.read(buffer)) != -1) {
                    fos.write(buffer, 0, length);
                }
            }
            audioStream.close(); // 스트림 닫기

            // 3. S3에 업로드
            String uploadedUrl = s3Uploader.upload(tempAudioFile, s3Directory + s3FileName);
            log.info("TTS 오디오 S3 업로드 완료: {}", uploadedUrl);
            return uploadedUrl;

        } catch (IOException e) { // IOException을 명시적으로 catch
            log.error("AWS Polly TTS 생성 또는 S3 업로드 중 IOException 발생: {}", e.getMessage(), e);
            return null; 
        } catch (Exception e) { 
            log.error("AWS Polly TTS 생성 또는 S3 업로드 중 기타 오류 발생: {}", e.getMessage(), e);
            return null; 
        } finally {
            if (tempAudioFile != null && tempAudioFile.exists()) {
                if (!tempAudioFile.delete()) {
                    log.warn("임시 오디오 파일 삭제 실패: {}", tempAudioFile.getAbsolutePath());
                }
            }
        }
    }
} 