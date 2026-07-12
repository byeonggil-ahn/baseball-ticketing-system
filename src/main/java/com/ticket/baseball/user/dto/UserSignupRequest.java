package com.ticket.baseball.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;


// 회원가입 요청 데이터
// (NotNull은 공백 "" ," " 같은 데이터까지 허용하기 때문에 NotBlank 사용)
// Validation 실패 시 사용할 상세 메시지를 정의
// 현재는 GlobalExceptionHandler에서 공통 메시지로 응답하지만,
// 추후 상세 에러 응답 또는 로그 출력 시 활용 가능
@Getter
@NoArgsConstructor
public class UserSignupRequest {


    // 이메일 형식 및 빈 값 체크
    @Email(message = "이메일 형식이 올바르지 않습니다.")
    @NotBlank(message = "이메일은 필수 입력입니다.")
    private String email;


    // 비밀번호 빈 값 체크
    @NotBlank(message = "비밀번호는 필수 입력입니다.")
    private String password;


    // 이름 빈 값 체크
    @NotBlank(message = "이름은 필수 입력입니다.")
    private String name;
}
