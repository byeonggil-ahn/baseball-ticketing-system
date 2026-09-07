package com.ticket.baseball.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;


// 회원가입 요청 데이터
@Getter
@NoArgsConstructor
public class UserSignupRequest {

    // 로그인 아이디
    @NotBlank(message = "로그인 아이디는 필수 입력입니다.")
    private String loginId;

    // 비밀번호
    @NotBlank(message = "비밀번호는 필수 입력입니다.")
    private String password;

    // 닉네임
    @NotBlank(message = "닉네임은 필수 입력입니다.")
    private String nickname;

    // 이메일
    @Email(message = "이메일 형식이 올바르지 않습니다.")
    @NotBlank(message = "이메일은 필수 입력입니다.")
    private String email;
}