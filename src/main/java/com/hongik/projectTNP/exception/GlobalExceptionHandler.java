package com.hongik.projectTNP.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import java.util.stream.Collectors;

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

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorResponse> handleIllegalStateException(IllegalStateException ex) {
        log.error("IllegalStateException 발생: {}", ex.getMessage(), ex);
        ErrorResponse errorResponse = new ErrorResponse(400, ex.getMessage());
        return ResponseEntity.badRequest().body(errorResponse);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException ex) {
        String errorMessage = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getDefaultMessage())
                .collect(Collectors.joining(", "));
        
        log.error("Validation 오류: {}", errorMessage);
        ErrorResponse errorResponse = new ErrorResponse(400, errorMessage);
        return ResponseEntity.badRequest().body(errorResponse);
    }

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