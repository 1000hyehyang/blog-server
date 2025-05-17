package com.thousandhyehyang.blog.config;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermission;
import java.util.HashSet;
import java.util.Set;

@Configuration
public class FileUploadSecurityConfig implements WebMvcConfigurer {

    @Value("${app.upload.temp-dir:${java.io.tmpdir}/uploads}")
    private String tempUploadDir;

    /**
     * 임시 업로드 디렉토리를 생성하고 적절한 권한을 설정합니다.
     * S3/R2에 업로드되기 전까지 임시 저장용으로 사용됩니다.
     */
    @PostConstruct
    public void configureTempUploadDirectory() {
        Path uploadPath = Paths.get(tempUploadDir);

        try {
            // 디렉토리가 존재하지 않으면 생성
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
                System.out.println("임시 업로드 디렉토리 생성됨: " + uploadPath);
            }

            // 디렉토리 접근 권한 설정 (실행 권한 제거)
            File uploadDir = uploadPath.toFile();

            // Windows인 경우 setReadable/setWritable 사용
            if (System.getProperty("os.name").toLowerCase().contains("win")) {
                uploadDir.setReadable(true);
                uploadDir.setWritable(true);
                uploadDir.setExecutable(false);
            } else {
                // Unix 기반 OS인 경우 POSIX 권한 사용
                try {
                    Set<PosixFilePermission> permissions = new HashSet<>();
                    permissions.add(PosixFilePermission.OWNER_READ);
                    permissions.add(PosixFilePermission.OWNER_WRITE);
                    permissions.add(PosixFilePermission.GROUP_READ);
                    Files.setPosixFilePermissions(uploadPath, permissions);
                } catch (UnsupportedOperationException e) {
                    // POSIX 권한 설정이 불가능한 경우 Fallback
                    uploadDir.setReadable(true);
                    uploadDir.setWritable(true);
                    uploadDir.setExecutable(false);
                }
            }

            System.out.println("임시 업로드 디렉토리 권한 설정 완료: " + uploadPath);
        } catch (IOException e) {
            throw new RuntimeException("업로드 디렉토리 초기화 실패", e);
        }
    }

    /**
     * 업로드된 파일이 저장되는 경로에 대해 정적 자원 핸들러를 설정합니다.
     * 이 핸들러는 업로드된 파일의 실행을 방지합니다.
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + tempUploadDir + "/")
                .setCachePeriod(3600)
                .resourceChain(true);
    }
}