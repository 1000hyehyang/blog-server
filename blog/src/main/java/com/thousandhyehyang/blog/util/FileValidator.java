package com.thousandhyehyang.blog.util;

import com.thousandhyehyang.blog.enums.UploadType;
import com.thousandhyehyang.blog.exception.FileUploadException;
import org.apache.tika.Tika;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public class FileValidator {

    private static final Tika tika = new Tika();

    private static final List<String> IMAGE_TYPES = List.of("image/jpeg", "image/png", "image/webp", "image/gif");
    private static final List<String> VIDEO_TYPES = List.of("video/mp4", "video/webm");
    private static final List<String> DOCUMENT_TYPES = List.of(
            "application/pdf", 
            "application/msword", 
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "application/vnd.ms-excel",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            "application/vnd.ms-powerpoint",
            "application/vnd.openxmlformats-officedocument.presentationml.presentation",
            "text/plain"
    );

    private static final List<String> IMAGE_EXTS = List.of("jpg", "jpeg", "png", "webp", "gif");
    private static final List<String> VIDEO_EXTS = List.of("mp4", "webm");
    private static final List<String> DOCUMENT_EXTS = List.of("pdf", "doc", "docx", "xls", "xlsx", "ppt", "pptx", "txt");

    private static final long MAX_THUMBNAIL_SIZE = 2 * 1024 * 1024; // 썸네일용 2MB
    private static final long MAX_EDITOR_IMAGE_SIZE = 5 * 1024 * 1024; // 에디터 이미지용 5MB
    private static final long MAX_VIDEO_SIZE = 50 * 1024 * 1024; // 비디오용 50MB
    private static final long MAX_DOCUMENT_SIZE = 10 * 1024 * 1024; // 문서용 10MB

    public static void validateByType(MultipartFile file, UploadType type) {
        if (file == null || file.isEmpty()) throw new FileUploadException("파일이 비어 있습니다.");

        String declaredContentType = file.getContentType();
        String ext = getExtension(file.getOriginalFilename());

        // Apache Tika를 사용하여 실제 MIME 타입 감지
        String actualContentType;
        try {
            actualContentType = tika.detect(file.getInputStream());
        } catch (IOException e) {
            throw new FileUploadException("파일 타입 감지 중 오류가 발생했습니다: " + e.getMessage());
        }

        // 선언된 콘텐츠 타입이 실제 콘텐츠 타입과 일치하는지 확인
        if (!declaredContentType.equals(actualContentType)) {
            throw new FileUploadException("파일 MIME 타입이 일치하지 않습니다. 선언된 타입: " + declaredContentType + ", 실제 타입: " + actualContentType);
        }

        switch (type) {
            case THUMBNAIL -> {
                if (!IMAGE_TYPES.contains(actualContentType)) throw new FileUploadException("이미지 MIME 타입이 잘못되었습니다.");
                if (!IMAGE_EXTS.contains(ext)) throw new FileUploadException("지원하지 않는 이미지 확장자입니다.");
                if (file.getSize() > MAX_THUMBNAIL_SIZE) throw new FileUploadException("썸네일 이미지 크기는 2MB 이하만 허용됩니다.");
            }
            case EDITOR_IMAGE -> {
                if (!IMAGE_TYPES.contains(actualContentType)) throw new FileUploadException("이미지 MIME 타입이 잘못되었습니다.");
                if (!IMAGE_EXTS.contains(ext)) throw new FileUploadException("지원하지 않는 이미지 확장자입니다.");
                if (file.getSize() > MAX_EDITOR_IMAGE_SIZE) throw new FileUploadException("본문 이미지 크기는 5MB 이하만 허용됩니다.");
            }
            case EDITOR_VIDEO -> {
                if (!VIDEO_TYPES.contains(actualContentType)) throw new FileUploadException("비디오 MIME 타입이 잘못되었습니다.");
                if (!VIDEO_EXTS.contains(ext)) throw new FileUploadException("지원하지 않는 비디오 확장자입니다.");
                if (file.getSize() > MAX_VIDEO_SIZE) throw new FileUploadException("비디오는 50MB 이하만 허용됩니다.");
            }
            case DOCUMENT -> {
                if (!DOCUMENT_TYPES.contains(actualContentType)) throw new FileUploadException("문서 MIME 타입이 잘못되었습니다.");
                if (!DOCUMENT_EXTS.contains(ext)) throw new FileUploadException("지원하지 않는 문서 확장자입니다.");
                if (file.getSize() > MAX_DOCUMENT_SIZE) throw new FileUploadException("문서는 10MB 이하만 허용됩니다.");
            }
        }
    }

    private static String getExtension(String filename) {
        int lastDot = filename.lastIndexOf(".");
        return lastDot != -1 ? filename.substring(lastDot + 1).toLowerCase() : "";
    }
}
