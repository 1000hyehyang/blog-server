package com.thousandhyehyang.blog.service;

import com.thousandhyehyang.blog.entity.Account;
import com.thousandhyehyang.blog.repository.AccountRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AccountService {

    private final AccountRepository accountRepository;

    public AccountService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    /**
     * 계정의 프로필 정보를 업데이트합니다.
     *
     * @param accountId    업데이트할 계정의 ID
     * @param name         새 이름
     * @param profileImage 새 프로필 이미지 URL
     * @return 업데이트된 계정
     * @throws IllegalArgumentException 계정을 찾을 수 없는 경우
     */
    @Transactional
    public Account updateProfile(Long accountId, String name, String profileImage) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new IllegalArgumentException("Account not found with ID: " + accountId));

        // 비즈니스 로직: 프로필 정보 업데이트
        account.updateProfile(name, profileImage);

        return accountRepository.save(account);
    }

    /**
     * 계정의 닉네임을 업데이트합니다.
     *
     * @param accountId 업데이트할 계정의 ID
     * @param nickname  새 닉네임
     * @return 업데이트된 계정
     * @throws IllegalArgumentException 계정을 찾을 수 없는 경우
     */
    @Transactional
    public Account updateNickname(Long accountId, String nickname) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new IllegalArgumentException("Account not found with ID: " + accountId));

        // 비즈니스 로직: 닉네임 업데이트
        account.updateNickname(nickname);

        return accountRepository.save(account);
    }

    /**
     * ID로 계정을 조회합니다.
     *
     * @param accountId 조회할 계정의 ID
     * @return 계정
     * @throws IllegalArgumentException 계정을 찾을 수 없는 경우
     */
    @Transactional(readOnly = true)
    public Account getAccountById(Long accountId) {
        return accountRepository.findById(accountId)
                .orElseThrow(() -> new IllegalArgumentException("Account not found with ID: " + accountId));
    }
}