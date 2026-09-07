package com.ticket.baseball.user.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;


// 현재 로그인한 회원 정보 응답 데이터
@Getter
@AllArgsConstructor
public class UserInfoResponse {

    // 회원 고유 번호
    private Long id;

    // 회원 이메일
    private String email;

    // 회원 닉네임
    private String nickname;
}