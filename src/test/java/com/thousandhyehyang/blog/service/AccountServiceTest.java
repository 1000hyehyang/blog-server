package com.thousandhyehyang.blog.service;

import com.thousandhyehyang.blog.entity.Account;
import com.thousandhyehyang.blog.repository.AccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @InjectMocks
    private AccountService accountService;

    private Account testAccount;

    @BeforeEach
    void setUp() {
        // 테스트용 계정 설정
        testAccount = new Account(
                "google",
                "test@example.com",
                "테스트 사용자",
                "테스트닉네임",
                "https://example.com/profile.jpg",
                Account.Role.USER
        );
        
        // ID 모킹
        when(testAccount.getId()).thenReturn(1L);
    }

    @Test
    @DisplayName("계정_조회_성공")
    void 계정_조회_성공() {
        // given
        given(accountRepository.findById(anyLong())).willReturn(Optional.of(testAccount));

        // when
        Account foundAccount = accountService.getAccountById(1L);

        // then
        assertThat(foundAccount).isNotNull();
        assertThat(foundAccount.getId()).isEqualTo(testAccount.getId());
        assertThat(foundAccount.getEmail()).isEqualTo(testAccount.getEmail());
        verify(accountRepository).findById(1L);
    }

    @Test
    @DisplayName("계정_조회_실패_존재하지_않는_계정")
    void 계정_조회_실패_존재하지_않는_계정() {
        // given
        given(accountRepository.findById(anyLong())).willReturn(Optional.empty());

        // when & then
        assertThrows(IllegalArgumentException.class, () -> accountService.getAccountById(999L));
        verify(accountRepository).findById(999L);
    }

    @Test
    @DisplayName("프로필_업데이트_성공")
    void 프로필_업데이트_성공() {
        // given
        given(accountRepository.findById(anyLong())).willReturn(Optional.of(testAccount));
        given(accountRepository.save(any(Account.class))).willReturn(testAccount);

        String newName = "새 이름";
        String newProfileImage = "https://example.com/new-profile.jpg";

        // when
        Account updatedAccount = accountService.updateProfile(1L, newName, newProfileImage);

        // then
        assertThat(updatedAccount).isNotNull();
        verify(testAccount).updateProfile(newName, newProfileImage);
        verify(accountRepository).save(testAccount);
    }

    @Test
    @DisplayName("프로필_업데이트_실패_존재하지_않는_계정")
    void 프로필_업데이트_실패_존재하지_않는_계정() {
        // given
        given(accountRepository.findById(anyLong())).willReturn(Optional.empty());

        // when & then
        assertThrows(IllegalArgumentException.class, 
                () -> accountService.updateProfile(999L, "새 이름", "https://example.com/new-profile.jpg"));
        verify(accountRepository).findById(999L);
        verify(accountRepository, never()).save(any(Account.class));
    }

    @Test
    @DisplayName("닉네임_업데이트_성공")
    void 닉네임_업데이트_성공() {
        // given
        given(accountRepository.findById(anyLong())).willReturn(Optional.of(testAccount));
        given(accountRepository.save(any(Account.class))).willReturn(testAccount);

        String newNickname = "새닉네임";

        // when
        Account updatedAccount = accountService.updateNickname(1L, newNickname);

        // then
        assertThat(updatedAccount).isNotNull();
        verify(testAccount).updateNickname(newNickname);
        verify(accountRepository).save(testAccount);
    }

    @Test
    @DisplayName("닉네임_업데이트_실패_존재하지_않는_계정")
    void 닉네임_업데이트_실패_존재하지_않는_계정() {
        // given
        given(accountRepository.findById(anyLong())).willReturn(Optional.empty());

        // when & then
        assertThrows(IllegalArgumentException.class, 
                () -> accountService.updateNickname(999L, "새닉네임"));
        verify(accountRepository).findById(999L);
        verify(accountRepository, never()).save(any(Account.class));
    }

    @Test
    @DisplayName("닉네임_업데이트_실패_중복된_닉네임")
    void 닉네임_업데이트_실패_중복된_닉네임() {
        // given
        given(accountRepository.findById(anyLong())).willReturn(Optional.of(testAccount));
        
        // updateNickname 메서드가 호출될 때 예외 발생하도록 설정
        doThrow(new IllegalArgumentException("이미 사용 중인 닉네임입니다."))
                .when(testAccount).updateNickname("중복닉네임");

        // when & then
        assertThrows(IllegalArgumentException.class, 
                () -> accountService.updateNickname(1L, "중복닉네임"));
        verify(accountRepository).findById(1L);
        verify(accountRepository, never()).save(any(Account.class));
    }
}