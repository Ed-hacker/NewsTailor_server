package com.hongik.projectTNP.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ErrorResponse> handleCustomException(CustomException ex) {
        log.error("CustomException 발생: {}", ex.getMessage(), ex);
        ErrorResponse errorResponse = new ErrorResponse(ex.getHttpStatus().value(), ex.getMessage());
        return new ResponseEntity<>(errorResponse, ex.getHttpStatus());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(IllegalArgumentException ex) {
        log.error("IllegalArgumentException 발생: {}", ex.getMessage(), ex);
        ErrorResponse errorResponse = new ErrorResponse(400, "잘못된 요청 파라미터입니다: " + ex.getMessage());
        return ResponseEntity.badRequest().body(errorResponse);
    }

    // 필요한 다른 예외 핸들러들 추가 가능 (e.g., MethodArgumentNotValidException 등)

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGlobalException(Exception ex) {
        log.error("처리되지 않은 예외 발생: {}", ex.getMessage(), ex);
        ErrorResponse errorResponse = new ErrorResponse(500, "서버 내부 오류가 발생했습니다.");
        return ResponseEntity.internalServerError().body(errorResponse);
    }

    // 간단한 에러 응답 DTO
    private static class ErrorResponse {
        private int status;
        private String message;

        public ErrorResponse(int status, String message) {
            this.status = status;
            this.message = message;
        }

        public int getStatus() {
            return status;
        }

        public String getMessage() {
            return message;
        }
    }
} 