package com.thousandhyehyang.blog.exception;

import com.thousandhyehyang.blog.common.ApiErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(FileUploadException.class)
    public ResponseEntity<ApiErrorResponse> handleFileUploadError(FileUploadException ex) {
        return ResponseEntity.badRequest().body(
                new ApiErrorResponse("INVALID_FILE", ex.getMessage())
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidationError(MethodArgumentNotValidException ex) {
        String errorMessage = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));

        return ResponseEntity.badRequest().body(
                new ApiErrorResponse("VALIDATION_ERROR", errorMessage)
        );
    }

    @ExceptionHandler(BindException.class)
    public ResponseEntity<ApiErrorResponse> handleBindError(BindException ex) {
        String errorMessage = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));

        return ResponseEntity.badRequest().body(
                new ApiErrorResponse("VALIDATION_ERROR", errorMessage)
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleGenericError(Exception ex, HttpServletRequest request) {
        String path = request.getRequestURI();

        // Skip exception handling for Swagger-related paths
        if (path.contains("/v3/api-docs") || path.contains("/swagger-ui")) {
            throw new RuntimeException(ex);
        }

        return ResponseEntity.internalServerError().body(
                new ApiErrorResponse("INTERNAL_ERROR", "알 수 없는 서버 오류가 발생했습니다.")
        );
    }
}
