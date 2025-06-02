package com.thousandhyehyang.blog.service.file;

import com.thousandhyehyang.blog.entity.FileMetadata;
import com.thousandhyehyang.blog.repository.FileMetadataRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 고아 파일 정리 서비스
 * 주기적으로 실행되어 어떤 게시글에도 연결되지 않은 파일을 정리합니다.
 */
@Service
public class FileCleanupService {

    private static final Logger log = LoggerFactory.getLogger(FileCleanupService.class);
    
    private final FileMetadataRepository fileMetadataRepository;
    private final FileUploadService fileUploadService;
    
    public FileCleanupService(FileMetadataRepository fileMetadataRepository, FileUploadService fileUploadService) {
        this.fileMetadataRepository = fileMetadataRepository;
        this.fileUploadService = fileUploadService;
    }
    
    /**
     * 고아 파일 정리 작업
     * 매일 새벽 3시에 실행되어 생성된 지 24시간이 지났으나 어떤 게시글에도 연결되지 않은 파일을 삭제합니다.
     */
    @Scheduled(cron = "0 0 3 * * ?") // 매일 새벽 3시에 실행
    @Transactional
    public void cleanupOrphanedFiles() {
        log.info("고아 파일 정리 작업 시작");
        
        try {
            // 24시간 전 시간 계산
            LocalDateTime cutoffDate = LocalDateTime.now().minusHours(24);
            
            // 고아 파일 조회
            List<FileMetadata> orphanedFiles = fileMetadataRepository.findOrphanedFiles(cutoffDate);
            
            if (orphanedFiles.isEmpty()) {
                log.info("삭제할 고아 파일이 없습니다.");
                return;
            }
            
            log.info("총 {}개의 고아 파일을 삭제합니다.", orphanedFiles.size());
            
            // 파일 삭제 처리
            for (FileMetadata file : orphanedFiles) {
                try {
                    // 스토리지에서 파일 삭제 및 메타데이터 삭제
                    fileUploadService.deleteFile(file.getId());
                    log.info("고아 파일 삭제 완료: ID={}, 파일명={}, 스토리지키={}", 
                            file.getId(), file.getOriginalFilename(), file.getStorageKey());
                } catch (Exception e) {
                    log.error("고아 파일 삭제 중 오류 발생: ID={}, 파일명={}", 
                            file.getId(), file.getOriginalFilename(), e);
                }
            }
            
            log.info("고아 파일 정리 작업 완료: {}개 파일 삭제됨", orphanedFiles.size());
        } catch (Exception e) {
            log.error("고아 파일 정리 작업 중 오류 발생", e);
        }
    }
    
    /**
     * 수동 고아 파일 정리 작업
     * 특정 기간(시간) 이전에 생성된 고아 파일을 삭제합니다.
     * 
     * @param hours 생성 후 경과 시간 (시간 단위)
     * @return 삭제된 파일 수
     */
    @Transactional
    public int cleanupOrphanedFiles(int hours) {
        log.info("수동 고아 파일 정리 작업 시작: {}시간 이전 파일 대상", hours);
        
        try {
            // 지정된 시간 전 계산
            LocalDateTime cutoffDate = LocalDateTime.now().minusHours(hours);
            
            // 고아 파일 조회
            List<FileMetadata> orphanedFiles = fileMetadataRepository.findOrphanedFiles(cutoffDate);
            
            if (orphanedFiles.isEmpty()) {
                log.info("삭제할 고아 파일이 없습니다.");
                return 0;
            }
            
            log.info("총 {}개의 고아 파일을 삭제합니다.", orphanedFiles.size());
            
            // 파일 삭제 처리
            int deletedCount = 0;
            for (FileMetadata file : orphanedFiles) {
                try {
                    // 스토리지에서 파일 삭제 및 메타데이터 삭제
                    fileUploadService.deleteFile(file.getId());
                    deletedCount++;
                    log.info("고아 파일 삭제 완료: ID={}, 파일명={}", 
                            file.getId(), file.getOriginalFilename());
                } catch (Exception e) {
                    log.error("고아 파일 삭제 중 오류 발생: ID={}, 파일명={}", 
                            file.getId(), file.getOriginalFilename(), e);
                }
            }
            
            log.info("수동 고아 파일 정리 작업 완료: {}개 파일 삭제됨", deletedCount);
            return deletedCount;
        } catch (Exception e) {
            log.error("수동 고아 파일 정리 작업 중 오류 발생", e);
            return 0;
        }
    }
}