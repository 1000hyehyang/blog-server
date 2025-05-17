package com.thousandhyehyang.blog.controller;

import ch.qos.logback.classic.Logger;
import com.thousandhyehyang.blog.common.ApiResponse;
import com.thousandhyehyang.blog.dto.account.NicknameUpdateRequest;
import com.thousandhyehyang.blog.entity.Account;
import com.thousandhyehyang.blog.repository.AccountRepository;
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
    private Logger log;

    public AccountController(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    /**
     * 닉네임 수정
     * @param request 새 닉네임 요청 DTO
     * @param account 인증된 사용자 계정
     */
    @PutMapping("/nickname")
    public ResponseEntity<ApiResponse<Map<String, String>>> updateNickname(
            @RequestBody NicknameUpdateRequest request, 
            @AuthenticationPrincipal Account account) {

        // 닉네임 중복 검사
        if (accountRepository.existsByNickname(request.getNickname())) {
            Account existingAccount = accountRepository.findByNickname(request.getNickname()).orElse(null);
            if (existingAccount != null && !existingAccount.getId().equals(account.getId())) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Nickname already taken"));
            }
        }

        account.updateNickname(request.getNickname());
        accountRepository.save(account);

        Map<String, String> data = new HashMap<>();
        data.put("nickname", account.getNickname());

        return ResponseEntity.ok(ApiResponse.success(data, "Nickname updated successfully"));
    }

    /**
     * 사용자 프로필 조회
     */
    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getProfile(@AuthenticationPrincipal Object principal) {
        System.out.println("✅ principal class = " + (principal != null ? principal.getClass().getName() : "null"));

        if (!(principal instanceof Account account)) {
            return ResponseEntity.status(401).body(ApiResponse.error("로그인이 필요합니다."));
        }

        Map<String, Object> data = new HashMap<>();
        data.put("id", account.getId());
        data.put("email", account.getEmail());
        data.put("name", account.getName());
        data.put("nickname", account.getNickname());
        data.put("profileImage", account.getProfileImage());
        data.put("role", account.getRole());

        return ResponseEntity.ok(ApiResponse.success(data));
    }

}
