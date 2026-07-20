package com.ticket.baseball.user.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 현재 로그인한 회원 정보를 응답하는 DTO

 * Entity를 그대로 반환하지 않고(정보 노출 방지),
 * 클라이언트에게 필요한 정보만 전달하기 위해 사용
 */
@Getter
@AllArgsConstructor
public class UserInfoResponse {

    // 회원 고유 번호
    private Long id;

    // 회원 이메일
    private String email;

    // 회원 이름
    private String name;
}