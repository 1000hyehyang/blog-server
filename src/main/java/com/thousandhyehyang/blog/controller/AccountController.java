package com.thousandhyehyang.blog.controller;

import ch.qos.logback.classic.Logger;
import com.thousandhyehyang.blog.common.ApiResponse;
import com.thousandhyehyang.blog.dto.account.NicknameUpdateRequest;
import com.thousandhyehyang.blog.entity.Account;
import com.thousandhyehyang.blog.repository.AccountRepository;
import com.thousandhyehyang.blog.service.account.AccountService;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/account")
public class AccountController {

    private final AccountRepository accountRepository;
    private final AccountService accountService;
    private Logger log;

    public AccountController(AccountRepository accountRepository, AccountService accountService) {
        this.accountRepository = accountRepository;
        this.accountService = accountService;
    }

    /**
     * 닉네임 수정
     *
     * @param request 새 닉네임 요청 DTO
     * @param account 인증된 사용자 계정
     */
    @PutMapping("/nickname")
    public ResponseEntity<ApiResponse<Map<String, String>>> updateNickname(
            @Valid @RequestBody NicknameUpdateRequest request,
            @AuthenticationPrincipal Account account) {

        // 닉네임 중복 검사
        if (accountRepository.existsByNickname(request.getNickname())) {
            Account existingAccount = accountRepository.findByNickname(request.getNickname()).orElse(null);
            if (existingAccount != null && !existingAccount.getId().equals(account.getId())) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Nickname already taken"));
            }
        }

        // 서비스 계층을 통해 닉네임 업데이트
        Account updatedAccount = accountService.updateNickname(account.getId(), request.getNickname());

        Map<String, String> data = new HashMap<>();
        data.put("nickname", updatedAccount.getNickname());

        return ResponseEntity.ok(ApiResponse.success(data, "Nickname updated successfully"));
    }

    /**
     * 사용자 프로필 조회
     */
    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getProfile(@AuthenticationPrincipal Object principal) {

        if (!(principal instanceof Account account)) {
            return ResponseEntity.status(401).body(ApiResponse.error("로그인이 필요합니다."));
        }

        // 서비스 계층을 통해 계정 정보 조회
        Account accountDetails = accountService.getAccountById(account.getId());

        Map<String, Object> data = new HashMap<>();
        data.put("id", accountDetails.getId());
        data.put("email", accountDetails.getEmail());
        data.put("nickname", accountDetails.getNickname());
        data.put("profileImage", accountDetails.getProfileImage());
        data.put("role", accountDetails.getRole());

        return ResponseEntity.ok(ApiResponse.success(data));
    }
}
