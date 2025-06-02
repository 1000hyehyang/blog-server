package com.thousandhyehyang.blog.exception;

import com.thousandhyehyang.blog.common.ApiErrorResponse;
import com.thousandhyehyang.blog.common.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

/**
 * 전역 예외 처리 클래스
 * 컨트롤러에서 발생하는 예외를 한 곳에서 처리하여 일관된 응답 포맷을 제공합니다.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 파일 업로드 실패 시 예외 처리
     */
    @ExceptionHandler(FileUploadException.class)
    public ResponseEntity<ApiErrorResponse> handleFileUploadError(FileUploadException ex) {
        return ResponseEntity.badRequest().body(
                ApiErrorResponse.of(ErrorCode.INVALID_FILE, ex.getMessage())
        );
    }

    /**
     * 게시글을 찾을 수 없을 때 발생하는 예외 처리
     */
    @ExceptionHandler(PostNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handlePostNotFound(PostNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                ApiErrorResponse.of(ErrorCode.POST_NOT_FOUND, ex.getMessage())
        );
    }

    /**
     * 게시글-파일 매핑이 중복될 경우 예외 처리
     */
    @ExceptionHandler(DuplicateFileMappingException.class)
    public ResponseEntity<ApiErrorResponse> handleDuplicateFileMapping(DuplicateFileMappingException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(
                ApiErrorResponse.of(ErrorCode.DUPLICATE_FILE_MAPPING, ex.getMessage())
        );
    }

    /**
     * 인증되지 않은 사용자의 접근 시 예외 처리
     */
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiErrorResponse> handleAuthenticationException(AuthenticationException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                ApiErrorResponse.of(ErrorCode.AUTHENTICATION_ERROR, ex.getMessage())
        );
    }

    /**
     * @Valid 유효성 검증 실패
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidationError(MethodArgumentNotValidException ex) {
        String errorMessage = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));

        return ResponseEntity.badRequest().body(
                ApiErrorResponse.of(ErrorCode.VALIDATION_ERROR, errorMessage)
        );
    }

    /**
     * 바인딩 예외 처리
     */
    @ExceptionHandler(BindException.class)
    public ResponseEntity<ApiErrorResponse> handleBindError(BindException ex) {
        String errorMessage = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));

        return ResponseEntity.badRequest().body(
                ApiErrorResponse.of(ErrorCode.VALIDATION_ERROR, errorMessage)
        );
    }

    /**
     * 이메일 중복 구독 시 예외 처리
     */
    @ExceptionHandler(DuplicateSubscriptionException.class)
    public ResponseEntity<ApiErrorResponse> handleDuplicateSubscriptionException(DuplicateSubscriptionException e) {
        log.error("Duplicate subscription: {}", e.getMessage());
        return ResponseEntity
                .status(ErrorCode.DUPLICATE_SUBSCRIPTION.getStatus())
                .body(ApiErrorResponse.of(ErrorCode.DUPLICATE_SUBSCRIPTION, e.getMessage()));
    }

    /**
     * 그 외 알 수 없는 예외 처리
     * Swagger 경로에서는 예외를 그대로 터뜨려 개발 환경에 영향이 없도록 함
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleGenericError(Exception ex, HttpServletRequest request) {
        String path = request.getRequestURI();

        // Skip exception handling for Swagger-related paths
        if (path.contains("/v3/api-docs") || path.contains("/swagger-ui")) {
            throw new RuntimeException(ex);
        }

        // 디버그 메시지로 예외 정보 추가
        return ResponseEntity.internalServerError().body(
                ApiErrorResponse.of(ErrorCode.INTERNAL_ERROR, ErrorCode.INTERNAL_ERROR.getDefaultMessage(), ex.getMessage())
        );
    }
}
