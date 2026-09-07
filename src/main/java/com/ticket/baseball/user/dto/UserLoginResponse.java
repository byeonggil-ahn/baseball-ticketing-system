package com.ticket.baseball.user.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;


// 로그인 응답 데이터
@Getter
@NoArgsConstructor
public class UserLoginResponse {

    // 사용자 고유 번호
    private Long id;

    // 닉네임
    private String nickname;

    // 이메일
    private String email;

    // JWT
    private String accessToken;

    public UserLoginResponse(Long id,
                             String nickname,
                             String email,
                             String accessToken) {
        this.id = id;
        this.nickname = nickname;
        this.email = email;
        this.accessToken = accessToken;
    }
}