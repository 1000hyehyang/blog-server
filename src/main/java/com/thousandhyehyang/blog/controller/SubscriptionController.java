package com.thousandhyehyang.blog.controller;

import com.thousandhyehyang.blog.dto.subscription.SubscriptionRequest;
import com.thousandhyehyang.blog.dto.subscription.SubscriptionResponse;
import com.thousandhyehyang.blog.service.subscription.SubscriptionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "구독 관리", description = "블로그 구독 관련 API")
@RestController
@RequestMapping("/api/subscriptions")
@RequiredArgsConstructor
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    @Operation(summary = "구독하기", description = "이메일을 통해 블로그 구독을 신청합니다.")
    @PostMapping
    public ResponseEntity<SubscriptionResponse> subscribe(@Valid @RequestBody SubscriptionRequest request) {
        subscriptionService.subscribe(request.getEmail());
        return ResponseEntity.ok(new SubscriptionResponse("구독이 완료되었습니다."));
    }

    @Operation(summary = "구독 취소", description = "이메일을 통해 블로그 구독을 취소합니다.")
    @DeleteMapping("/{email}")
    public ResponseEntity<SubscriptionResponse> unsubscribe(@PathVariable String email) {
        subscriptionService.unsubscribe(email);
        return ResponseEntity.ok(new SubscriptionResponse("구독이 해지되었습니다."));
    }
} 