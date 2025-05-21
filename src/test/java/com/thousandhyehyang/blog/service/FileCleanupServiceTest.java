package com.thousandhyehyang.blog.service;

import com.thousandhyehyang.blog.entity.FileMetadata;
import com.thousandhyehyang.blog.repository.FileMetadataRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FileCleanupServiceTest {

    @Mock
    private FileMetadataRepository fileMetadataRepository;

    @Mock
    private FileUploadService fileUploadService;

    @InjectMocks
    private FileCleanupService fileCleanupService;

    private List<FileMetadata> orphanedFiles;

    @BeforeEach
    void setUp() {
        // 테스트용 고아 파일 목록 설정
        orphanedFiles = new ArrayList<>();
        
        for (int i = 1; i <= 3; i++) {
            FileMetadata file = mock(FileMetadata.class);
            when(file.getId()).thenReturn((long) i);
            when(file.getOriginalFilename()).thenReturn("orphaned-file-" + i + ".jpg");
            when(file.getStorageKey()).thenReturn("thumbnails/orphaned-file-" + i + ".jpg");
            orphanedFiles.add(file);
        }
    }

    @Test
    @DisplayName("예약된_고아_파일_정리_성공")
    void 예약된_고아_파일_정리_성공() {
        // given
        given(fileMetadataRepository.findOrphanedFiles(any(LocalDateTime.class))).willReturn(orphanedFiles);
        doNothing().when(fileUploadService).deleteFile(anyLong());

        // when
        fileCleanupService.cleanupOrphanedFiles();

        // then
        verify(fileMetadataRepository).findOrphanedFiles(any(LocalDateTime.class));
        
        // 각 파일에 대해 삭제 메서드가 호출되었는지 확인
        for (FileMetadata file : orphanedFiles) {
            verify(fileUploadService).deleteFile(file.getId());
        }
    }

    @Test
    @DisplayName("예약된_고아_파일_정리_성공_삭제할_파일_없음")
    void 예약된_고아_파일_정리_성공_삭제할_파일_없음() {
        // given
        given(fileMetadataRepository.findOrphanedFiles(any(LocalDateTime.class))).willReturn(new ArrayList<>());

        // when
        fileCleanupService.cleanupOrphanedFiles();

        // then
        verify(fileMetadataRepository).findOrphanedFiles(any(LocalDateTime.class));
        verify(fileUploadService, never()).deleteFile(anyLong());
    }

    @Test
    @DisplayName("예약된_고아_파일_정리_일부_실패_처리")
    void 예약된_고아_파일_정리_일부_실패_처리() {
        // given
        given(fileMetadataRepository.findOrphanedFiles(any(LocalDateTime.class))).willReturn(orphanedFiles);
        
        // 첫 번째 파일 삭제 시 예외 발생
        doThrow(new RuntimeException("파일 삭제 실패")).when(fileUploadService).deleteFile(1L);
        
        // 나머지 파일은 정상 삭제
        doNothing().when(fileUploadService).deleteFile(2L);
        doNothing().when(fileUploadService).deleteFile(3L);

        // when
        fileCleanupService.cleanupOrphanedFiles();

        // then
        verify(fileMetadataRepository).findOrphanedFiles(any(LocalDateTime.class));
        
        // 모든 파일에 대해 삭제 시도가 이루어졌는지 확인
        verify(fileUploadService).deleteFile(1L);
        verify(fileUploadService).deleteFile(2L);
        verify(fileUploadService).deleteFile(3L);
    }

    @Test
    @DisplayName("수동_고아_파일_정리_성공")
    void 수동_고아_파일_정리_성공() {
        // given
        given(fileMetadataRepository.findOrphanedFiles(any(LocalDateTime.class))).willReturn(orphanedFiles);
        doNothing().when(fileUploadService).deleteFile(anyLong());

        // when
        int deletedCount = fileCleanupService.cleanupOrphanedFiles(12); // 12시간 이전 파일 삭제

        // then
        assertThat(deletedCount).isEqualTo(3); // 3개 파일 모두 삭제됨
        verify(fileMetadataRepository).findOrphanedFiles(any(LocalDateTime.class));
        
        // 각 파일에 대해 삭제 메서드가 호출되었는지 확인
        for (FileMetadata file : orphanedFiles) {
            verify(fileUploadService).deleteFile(file.getId());
        }
    }

    @Test
    @DisplayName("수동_고아_파일_정리_성공_삭제할_파일_없음")
    void 수동_고아_파일_정리_성공_삭제할_파일_없음() {
        // given
        given(fileMetadataRepository.findOrphanedFiles(any(LocalDateTime.class))).willReturn(new ArrayList<>());

        // when
        int deletedCount = fileCleanupService.cleanupOrphanedFiles(12); // 12시간 이전 파일 삭제

        // then
        assertThat(deletedCount).isZero(); // 삭제된 파일 없음
        verify(fileMetadataRepository).findOrphanedFiles(any(LocalDateTime.class));
        verify(fileUploadService, never()).deleteFile(anyLong());
    }

    @Test
    @DisplayName("수동_고아_파일_정리_일부_실패_처리")
    void 수동_고아_파일_정리_일부_실패_처리() {
        // given
        given(fileMetadataRepository.findOrphanedFiles(any(LocalDateTime.class))).willReturn(orphanedFiles);
        
        // 첫 번째 파일 삭제 시 예외 발생
        doThrow(new RuntimeException("파일 삭제 실패")).when(fileUploadService).deleteFile(1L);
        
        // 나머지 파일은 정상 삭제
        doNothing().when(fileUploadService).deleteFile(2L);
        doNothing().when(fileUploadService).deleteFile(3L);

        // when
        int deletedCount = fileCleanupService.cleanupOrphanedFiles(12); // 12시간 이전 파일 삭제

        // then
        assertThat(deletedCount).isEqualTo(2); // 2개 파일만 삭제됨
        verify(fileMetadataRepository).findOrphanedFiles(any(LocalDateTime.class));
        
        // 모든 파일에 대해 삭제 시도가 이루어졌는지 확인
        verify(fileUploadService).deleteFile(1L);
        verify(fileUploadService).deleteFile(2L);
        verify(fileUploadService).deleteFile(3L);
    }

    @Test
    @DisplayName("수동_고아_파일_정리_저장소_예외_처리")
    void 수동_고아_파일_정리_저장소_예외_처리() {
        // given
        given(fileMetadataRepository.findOrphanedFiles(any(LocalDateTime.class)))
                .thenThrow(new RuntimeException("데이터베이스 오류"));

        // when
        int deletedCount = fileCleanupService.cleanupOrphanedFiles(12); // 12시간 이전 파일 삭제

        // then
        assertThat(deletedCount).isZero(); // 예외로 인해 삭제된 파일 없음
        verify(fileMetadataRepository).findOrphanedFiles(any(LocalDateTime.class));
        verify(fileUploadService, never()).deleteFile(anyLong());
    }
}