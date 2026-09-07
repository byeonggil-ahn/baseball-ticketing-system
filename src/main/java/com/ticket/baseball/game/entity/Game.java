package com.ticket.baseball.game.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "games")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Game {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 홈팀
    @Column(nullable = false)
    private String homeTeam;

    // 원정팀
    @Column(nullable = false)
    private String awayTeam;

    // 경기장
    @Column(nullable = false)
    private String stadium;

    // 경기 일시
    @Column(nullable = false)
    private LocalDateTime gameDate;

    // 예약 시작 시간
    @Column(nullable = false)
    private LocalDateTime reservationStartAt;

    // 예약 종료 시간
    @Column(nullable = false)
    private LocalDateTime reservationEndAt;

    // 생성 시간
    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Builder
    public Game(String homeTeam,
                String awayTeam,
                String stadium,
                LocalDateTime gameDate,
                LocalDateTime reservationStartAt,
                LocalDateTime reservationEndAt,
                LocalDateTime createdAt) {

        this.homeTeam = homeTeam;
        this.awayTeam = awayTeam;
        this.stadium = stadium;
        this.gameDate = gameDate;
        this.reservationStartAt = reservationStartAt;
        this.reservationEndAt = reservationEndAt;
        this.createdAt = createdAt;
    }
}