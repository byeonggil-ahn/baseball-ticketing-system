package com.ticket.baseball.user.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;


// 로그인 요청 데이터
@Getter
@NoArgsConstructor
public class UserLoginRequest {

    // 로그인 아이디
    private String loginId;

    // 비밀번호
    private String password;

    public UserLoginRequest(String loginId, String password) {
        this.loginId = loginId;
        this.password = password;
    }
}