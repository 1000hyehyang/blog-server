package com.thousandhyehyang.blog.dto.account;

/**
 * 닉네임 변경 요청 DTO
 */
public class NicknameUpdateRequest {
    private String nickname;

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }
}