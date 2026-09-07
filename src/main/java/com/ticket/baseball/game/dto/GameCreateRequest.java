package com.ticket.baseball.game.dto;

import lombok.Getter;

import java.time.LocalDateTime;


// 경기 등록 요청 데이터
@Getter
public class GameCreateRequest {

    // 홈팀
    private String homeTeam;

    // 원정팀
    private String awayTeam;

    // 경기장
    private String stadium;

    // 경기 시간
    private LocalDateTime gameDate;

    // 예약 시작 시간
    private LocalDateTime reservationStartAt;

    // 예약 종료 시간
    private LocalDateTime reservationEndAt;
}