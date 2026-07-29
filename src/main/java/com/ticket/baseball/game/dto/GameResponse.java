package com.ticket.baseball.game.dto;

import com.ticket.baseball.game.entity.Game;
import lombok.Getter;

import java.time.LocalDateTime;


// 경기 응답 데이터
@Getter
public class GameResponse {

    private final Long id;
    private final String homeTeam;
    private final String awayTeam;
    private final String stadium;
    private final LocalDateTime gameDate;


    // Entity -> DTO 변환
    public GameResponse(Game game) {
        this.id = game.getId();
        this.homeTeam = game.getHomeTeam();
        this.awayTeam = game.getAwayTeam();
        this.stadium = game.getStadium();
        this.gameDate = game.getGameDate();
    }
}