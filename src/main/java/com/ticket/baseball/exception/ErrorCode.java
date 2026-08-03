package com.ticket.baseball.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

// 예외 상황별 코드 관리
@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    INVALID_INPUT("잘못된 요청입니다."),
    UNAUTHORIZED("인증이 필요합니다."),
    FORBIDDEN("접근 권한이 없습니다."),
    USER_NOT_FOUND("사용자를 찾을 수 없습니다."),
    DUPLICATE_USER("이미 존재하는 사용자입니다."),
    DUPLICATE_RESERVATION("이미 예약된 좌석입니다."),
    INTERNAL_SERVER_ERROR("서버 오류가 발생했습니다.");

    private final String message;
}