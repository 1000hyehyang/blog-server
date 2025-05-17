package com.thousandhyehyang.blog.controller;

import com.thousandhyehyang.blog.common.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * 서버 상태 확인을 위한 컨트롤러
 * 슬립 방지를 위한 간단한 ping 체크 API를 제공합니다.
 */
@RestController
@RequestMapping("/health")
public class HealthCheckController {

    /**
     * 서버 상태 확인 (ping)
     * @return 서버 상태 정보
     */
    @GetMapping("/ping")
    public ResponseEntity<ApiResponse<Map<String, Object>>> ping() {
        Map<String, Object> data = new HashMap<>();
        data.put("status", "UP");
        data.put("timestamp", System.currentTimeMillis());
        
        return ResponseEntity.ok(ApiResponse.success(data, "Server is running"));
    }
}