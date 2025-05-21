package com.thousandhyehyang.blog.service;

import com.thousandhyehyang.blog.common.ApiResponse;
import com.thousandhyehyang.blog.config.R2Properties;
import com.thousandhyehyang.blog.dto.FileUploadResponse;
import com.thousandhyehyang.blog.entity.FileMetadata;
import com.thousandhyehyang.blog.enums.UploadType;
import com.thousandhyehyang.blog.exception.FileUploadException;
import com.thousandhyehyang.blog.repository.FileMetadataRepository;
import com.thousandhyehyang.blog.util.FileValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FileUploadServiceTest {

    @Mock
    private S3Client s3Client;

    @Mock
    private R2Properties r2Properties;

    @Mock
    private FileMetadataRepository fileMetadataRepository;

    @Mock
    private MultipartFile mockFile;

    @InjectMocks
    private FileUploadService fileUploadService;

    private FileMetadata testFileMetadata;
    private byte[] testFileContent;

    @BeforeEach
    void setUp() {
        // R2Properties 설정
        when(r2Properties.getBucket()).thenReturn("test-bucket");
        when(r2Properties.getPublicUrl()).thenReturn("https://test-cdn.example.com");
        when(r2Properties.getThumbnailPath()).thenReturn("thumbnails/");
        when(r2Properties.getEditorImagePath()).thenReturn("editor-images/");
        when(r2Properties.getEditorVideoPath()).thenReturn("editor-videos/");
        when(r2Properties.getDocumentPath()).thenReturn("documents/");

        // 테스트 파일 설정
        testFileContent = "테스트 파일 내용".getBytes();
        when(mockFile.getOriginalFilename()).thenReturn("test-image.jpg");
        when(mockFile.getContentType()).thenReturn("image/jpeg");
        try {
            when(mockFile.getBytes()).thenReturn(testFileContent);
            when(mockFile.getInputStream()).thenReturn(new ByteArrayInputStream(testFileContent));
        } catch (IOException e) {
            throw new RuntimeException("테스트 설정 중 오류 발생", e);
        }
        when(mockFile.getSize()).thenReturn(1024L); // 1KB

        // 테스트 파일 메타데이터 설정
        testFileMetadata = mock(FileMetadata.class);
        when(testFileMetadata.getId()).thenReturn(1L);
        when(testFileMetadata.getStorageKey()).thenReturn("thumbnails/test-uuid.jpg");
        when(testFileMetadata.getPublicUrl()).thenReturn("https://test-cdn.example.com/thumbnails/test-uuid.jpg");
        when(testFileMetadata.getOriginalFilename()).thenReturn("test-image.jpg");
        when(testFileMetadata.getContentType()).thenReturn("image/jpeg");
    }

    @Test
    @DisplayName("파일_업로드_성공")
    void 파일_업로드_성공() {
        // given
        try (MockedStatic<FileValidator> fileValidatorMock = mockStatic(FileValidator.class)) {
            // FileValidator 모킹
            fileValidatorMock.when(() -> FileValidator.validateByType(any(MultipartFile.class), any(UploadType.class)))
                    .thenReturn(null); // void 메서드는 null 반환

            // UUID 생성 결과를 예측할 수 없으므로 S3Client의 putObject 호출 자체를 검증
            doNothing().when(s3Client).putObject(any(PutObjectRequest.class), any(RequestBody.class));

            // 파일 메타데이터 저장 모킹
            when(fileMetadataRepository.save(any(FileMetadata.class))).thenReturn(testFileMetadata);

            // when
            ApiResponse<FileUploadResponse> response = fileUploadService.upload(mockFile, UploadType.THUMBNAIL);

            // then
            assertThat(response).isNotNull();
            assertThat(response.getData()).isNotNull();
            assertThat(response.getData().url()).isNotNull();
            assertThat(response.getData().type()).isEqualTo(UploadType.THUMBNAIL.name());

            // 메서드 호출 검증
            fileValidatorMock.verify(() -> FileValidator.validateByType(mockFile, UploadType.THUMBNAIL));
            verify(s3Client).putObject(any(PutObjectRequest.class), any(RequestBody.class));
            verify(fileMetadataRepository).save(any(FileMetadata.class));
        }
    }

    @Test
    @DisplayName("파일_업로드_실패_유효성검사_오류")
    void 파일_업로드_실패_유효성검사_오류() {
        // given
        try (MockedStatic<FileValidator> fileValidatorMock = mockStatic(FileValidator.class)) {
            // FileValidator가 예외를 던지도록 설정
            fileValidatorMock.when(() -> FileValidator.validateByType(any(MultipartFile.class), any(UploadType.class)))
                    .thenThrow(new FileUploadException("파일 유효성 검사 실패"));

            // when & then
            assertThrows(FileUploadException.class, () -> fileUploadService.upload(mockFile, UploadType.THUMBNAIL));

            // 메서드 호출 검증
            fileValidatorMock.verify(() -> FileValidator.validateByType(mockFile, UploadType.THUMBNAIL));
            verify(s3Client, never()).putObject(any(PutObjectRequest.class), any(RequestBody.class));
            verify(fileMetadataRepository, never()).save(any(FileMetadata.class));
        }
    }

    @Test
    @DisplayName("파일_다운로드_성공")
    void 파일_다운로드_성공() throws IOException {
        // given
        given(fileMetadataRepository.findById(anyLong())).willReturn(Optional.of(testFileMetadata));

        // S3 응답 모킹
        ResponseInputStream<GetObjectResponse> mockResponse = mock(ResponseInputStream.class);
        when(mockResponse.readAllBytes()).thenReturn(testFileContent);

        when(s3Client.getObject(any(GetObjectRequest.class))).thenReturn(mockResponse);

        // when
        byte[] downloadedContent = fileUploadService.downloadFile(1L);

        // then
        assertThat(downloadedContent).isEqualTo(testFileContent);
        verify(fileMetadataRepository).findById(1L);
        verify(s3Client).getObject(any(GetObjectRequest.class));
    }

    @Test
    @DisplayName("파일_다운로드_실패_존재하지_않는_파일")
    void 파일_다운로드_실패_존재하지_않는_파일() {
        // given
        given(fileMetadataRepository.findById(anyLong())).willReturn(Optional.empty());

        // when & then
        assertThrows(FileUploadException.class, () -> fileUploadService.downloadFile(999L));
        verify(fileMetadataRepository).findById(999L);
        verify(s3Client, never()).getObject(any(GetObjectRequest.class));
    }

    @Test
    @DisplayName("파일_메타데이터_조회_성공")
    void 파일_메타데이터_조회_성공() {
        // given
        given(fileMetadataRepository.findById(anyLong())).willReturn(Optional.of(testFileMetadata));

        // when
        FileMetadata metadata = fileUploadService.getFileMetadata(1L);

        // then
        assertThat(metadata).isEqualTo(testFileMetadata);
        verify(fileMetadataRepository).findById(1L);
    }

    @Test
    @DisplayName("파일_메타데이터_조회_실패_존재하지_않는_파일")
    void 파일_메타데이터_조회_실패_존재하지_않는_파일() {
        // given
        given(fileMetadataRepository.findById(anyLong())).willReturn(Optional.empty());

        // when & then
        assertThrows(FileUploadException.class, () -> fileUploadService.getFileMetadata(999L));
        verify(fileMetadataRepository).findById(999L);
    }

    @Test
    @DisplayName("파일_삭제_성공")
    void 파일_삭제_성공() {
        // given
        given(fileMetadataRepository.findById(anyLong())).willReturn(Optional.of(testFileMetadata));
        doNothing().when(s3Client).deleteObject(any(software.amazon.awssdk.services.s3.model.DeleteObjectRequest.class));

        // when
        fileUploadService.deleteFile(1L);

        // then
        verify(fileMetadataRepository).findById(1L);
        verify(s3Client).deleteObject(any(software.amazon.awssdk.services.s3.model.DeleteObjectRequest.class));
        verify(fileMetadataRepository).delete(testFileMetadata);
    }

    @Test
    @DisplayName("파일_삭제_실패_존재하지_않는_파일")
    void 파일_삭제_실패_존재하지_않는_파일() {
        // given
        given(fileMetadataRepository.findById(anyLong())).willReturn(Optional.empty());

        // when & then
        assertThrows(FileUploadException.class, () -> fileUploadService.deleteFile(999L));
        verify(fileMetadataRepository).findById(999L);
        verify(s3Client, never()).deleteObject(any(software.amazon.awssdk.services.s3.model.DeleteObjectRequest.class));
        verify(fileMetadataRepository, never()).delete(any(FileMetadata.class));
    }
}
