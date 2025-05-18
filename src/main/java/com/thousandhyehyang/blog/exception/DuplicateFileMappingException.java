package com.thousandhyehyang.blog.exception;

/**
 * 이미 존재하는 게시글-파일 매핑을 중복 생성하려고 할 때 발생하는 예외
 */
public class DuplicateFileMappingException extends RuntimeException {

    public DuplicateFileMappingException(String message) {
        super(message);
    }

    public DuplicateFileMappingException(Long postId, Long fileId, String referenceType) {
        super(String.format("Duplicate file mapping: post_id=%d, file_id=%d, reference_type=%s", 
                postId, fileId, referenceType));
    }
}