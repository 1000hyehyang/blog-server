package com.thousandhyehyang.blog.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 스케줄링 기능을 활성화하는 설정 클래스
 * Spring의 @Scheduled 어노테이션을 사용한 주기적 작업 실행을 지원합니다.
 */
@Configuration
@EnableScheduling
public class SchedulingConfig {
    // 스케줄링 관련 추가 설정이 필요한 경우 여기에 구현
}