package com.thousandhyehyang.blog.controller;

import com.thousandhyehyang.blog.common.ApiResponse;
import com.thousandhyehyang.blog.dto.FileUploadResponse;
import com.thousandhyehyang.blog.entity.FileMetadata;
import com.thousandhyehyang.blog.enums.UploadType;
import com.thousandhyehyang.blog.service.FileUploadService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/files")
@Tag(name = "파일 API", description = "파일 업로드, 다운로드, 삭제 관련 API")
public class FileUploadController {

    private final FileUploadService fileUploadService;

    public FileUploadController(FileUploadService fileUploadService) {
        this.fileUploadService = fileUploadService;
    }

    @Operation(
            summary = "썸네일 업로드",
            description = "썸네일 이미지를 업로드합니다. (jpg, jpeg, png, webp, gif, 최대 2MB)"
    )
    @PostMapping("/thumbnail")
    public ResponseEntity<ApiResponse<FileUploadResponse>> uploadThumbnail(
            @Parameter(description = "업로드할 썸네일 이미지 파일 (jpg, jpeg, png, webp, gif, 최대 2MB)", required = true)
            @RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(fileUploadService.upload(file, UploadType.THUMBNAIL));
    }

    @Operation(
            summary = "본문 이미지 업로드",
            description = "Tiptap 본문용 이미지를 업로드합니다. (jpg, jpeg, png, webp, gif, 최대 5MB)"
    )
    @PostMapping("/editor-image")
    public ResponseEntity<ApiResponse<FileUploadResponse>> uploadEditorImage(
            @Parameter(description = "업로드할 본문 이미지 파일 (jpg, jpeg, png, webp, gif, 최대 5MB)", required = true)
            @RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(fileUploadService.upload(file, UploadType.EDITOR_IMAGE));
    }

    @Operation(
            summary = "본문 영상 업로드",
            description = "Tiptap 본문용 영상을 업로드합니다. (mp4, webm, 최대 50MB)"
    )
    @PostMapping("/editor-video")
    public ResponseEntity<ApiResponse<FileUploadResponse>> uploadEditorVideo(
            @Parameter(description = "업로드할 본문 영상 파일 (mp4, webm, 최대 50MB)", required = true)
            @RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(fileUploadService.upload(file, UploadType.EDITOR_VIDEO));
    }

    @Operation(
            summary = "문서 업로드",
            description = "문서 파일을 업로드합니다. (pdf, doc, docx, xls, xlsx, ppt, pptx, txt, 최대 10MB)"
    )
    @PostMapping("/document")
    public ResponseEntity<ApiResponse<FileUploadResponse>> uploadDocument(
            @Parameter(description = "업로드할 문서 파일 (pdf, doc, docx, xls, xlsx, ppt, pptx, txt, 최대 10MB)", required = true)
            @RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(fileUploadService.upload(file, UploadType.DOCUMENT));
    }

    @Operation(
            summary = "파일 다운로드",
            description = "파일 ID로 파일을 다운로드합니다."
    )
    @GetMapping("/{id}/download")
    public ResponseEntity<Resource> downloadFile(
            @Parameter(description = "다운로드할 파일의 ID", required = true)
            @PathVariable Long id) {
        FileMetadata metadata = fileUploadService.getFileMetadata(id);
        byte[] fileContent = fileUploadService.downloadFile(id);

        ByteArrayResource resource = new ByteArrayResource(fileContent);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(metadata.getContentType()))
                .contentLength(metadata.getFileSize())
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + metadata.getOriginalFilename() + "\"")
                .body(resource);
    }

    @Operation(
            summary = "파일 삭제",
            description = "파일 ID로 파일을 삭제합니다."
    )
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFile(
            @Parameter(description = "삭제할 파일의 ID", required = true)
            @PathVariable Long id) {
        fileUploadService.deleteFile(id);
        return ResponseEntity.noContent().build();
    }
}
