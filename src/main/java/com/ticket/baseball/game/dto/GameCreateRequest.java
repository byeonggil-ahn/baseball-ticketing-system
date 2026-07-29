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
}