package com.thousandhyehyang.blog.exception;

/**
 * 파일 업로드 과정에서 발생하는 예외
 * 파일 업로드, 다운로드, 삭제 등의 작업 중 오류가 발생했을 때 사용됩니다.
 */
public class FileUploadException extends RuntimeException {
    public FileUploadException(String message) {
        super(message);
    }
}
