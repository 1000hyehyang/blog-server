package com.thousandhyehyang.blog.service;

import com.thousandhyehyang.blog.config.R2Properties;
import com.thousandhyehyang.blog.dto.FileUploadResponse;
import com.thousandhyehyang.blog.entity.FileMetadata;
import com.thousandhyehyang.blog.enums.UploadType;
import com.thousandhyehyang.blog.exception.FileUploadException;
import com.thousandhyehyang.blog.repository.FileMetadataRepository;
import com.thousandhyehyang.blog.util.FileValidator;
import com.thousandhyehyang.blog.common.ApiResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

@Service
public class FileUploadService {

    private final S3Client s3Client;
    private final R2Properties r2Properties;
    private final FileMetadataRepository fileMetadataRepository;

    public FileUploadService(S3Client s3Client, R2Properties r2Properties, FileMetadataRepository fileMetadataRepository) {
        this.s3Client = s3Client;
        this.r2Properties = r2Properties;
        this.fileMetadataRepository = fileMetadataRepository;
    }

    @Transactional
    public ApiResponse<FileUploadResponse> upload(MultipartFile file, UploadType type) {
        FileValidator.validateByType(file, type);

        String originalFilename = file.getOriginalFilename();
        String extension = sanitizeFilename(originalFilename);

        // 동일한 이름의 파일이 이미 존재하는지 확인
        Optional<FileMetadata> existingFile = fileMetadataRepository.findTopByOriginalFilenameOrderByVersionDesc(originalFilename);

        // 파일용 UUID 생성
        String uuid = UUID.randomUUID().toString();

        // 업로드 타입에 따른 적절한 접두사 가져오기
        String prefix = switch (type) {
            case THUMBNAIL -> r2Properties.getThumbnailPath();
            case EDITOR_IMAGE -> r2Properties.getEditorImagePath();
            case EDITOR_VIDEO -> r2Properties.getEditorVideoPath();
            case DOCUMENT -> r2Properties.getDocumentPath();
        };

        // 저장소 키 생성
        String key = prefix + uuid + (extension.isEmpty() ? "" : "." + extension);

        // S3/R2에 파일 업로드
        try {
            s3Client.putObject(PutObjectRequest.builder()
                    .bucket(r2Properties.getBucket())
                    .key(key)
                    .contentType(file.getContentType())
                    .build(), RequestBody.fromBytes(file.getBytes()));
        } catch (IOException e) {
            throw new FileUploadException("파일 업로드 중 오류 발생: " + e.getMessage());
        }

        // 공개 URL 생성
        String url = r2Properties.getPublicUrl() + "/" + key;

        // 파일 메타데이터 생성 및 저장
        FileMetadata metadata = new FileMetadata(
                originalFilename,
                key,
                file.getContentType(),
                file.getSize(),
                type,
                url
        );

        // 버전 관리
        if (existingFile.isPresent()) {
            FileMetadata previousVersion = existingFile.get();
            metadata.setVersion(previousVersion.getVersion() + 1);
            metadata.setPreviousVersionId(previousVersion.getId());
        }

        // 데이터베이스에 메타데이터 저장
        fileMetadataRepository.save(metadata);

        return new ApiResponse<>(new FileUploadResponse(url, type.name(), originalFilename));
    }

    private String sanitizeFilename(String filename) {
        if (filename == null || filename.isEmpty()) {
            return "";
        }

        // 확장자만 추출
        String ext = "";
        int lastDotIndex = filename.lastIndexOf(".");
        if (lastDotIndex > 0) {
            ext = filename.substring(lastDotIndex + 1).toLowerCase();
        }

        // 확장자 검증 (영숫자만 허용)
        if (!ext.matches("^[a-zA-Z0-9]+$")) {
            ext = "";
        }

        return ext;
    }

    /**
     * ID로 파일 다운로드
     * 
     * @param id 다운로드할 파일의 ID
     * @return 파일 내용과 메타데이터
     * @throws FileUploadException 파일을 찾을 수 없는 경우
     */
    public byte[] downloadFile(Long id) {
        FileMetadata metadata = fileMetadataRepository.findById(id)
                .orElseThrow(() -> new FileUploadException("파일을 찾을 수 없습니다."));

        try {
            // Get the file from S3/R2
            return s3Client.getObject(req -> req
                    .bucket(r2Properties.getBucket())
                    .key(metadata.getStorageKey())
                    .build())
                    .readAllBytes();
        } catch (IOException e) {
            throw new FileUploadException("파일 다운로드 중 오류 발생: " + e.getMessage());
        }
    }

    /**
     * ID로 파일 메타데이터 조회
     * 
     * @param id 파일의 ID
     * @return 파일 메타데이터
     * @throws FileUploadException 파일을 찾을 수 없는 경우
     */
    public FileMetadata getFileMetadata(Long id) {
        return fileMetadataRepository.findById(id)
                .orElseThrow(() -> new FileUploadException("파일을 찾을 수 없습니다."));
    }

    /**
     * ID로 파일 삭제
     * 
     * @param id 삭제할 파일의 ID
     * @throws FileUploadException 파일을 찾을 수 없는 경우
     */
    @Transactional
    public void deleteFile(Long id) {
        FileMetadata metadata = fileMetadataRepository.findById(id)
                .orElseThrow(() -> new FileUploadException("파일을 찾을 수 없습니다."));

        // S3/R2에서 파일 삭제
        s3Client.deleteObject(req -> req
                .bucket(r2Properties.getBucket())
                .key(metadata.getStorageKey())
                .build());

        // 데이터베이스에서 메타데이터 삭제
        fileMetadataRepository.delete(metadata);
    }
}
