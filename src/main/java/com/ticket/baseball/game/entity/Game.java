package com.ticket.baseball.game.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

// Game 객체를 DB의 games 테이블과 연결
@Entity
@Table(name = "games")

// Getter 자동 생성
@Getter

// 기본 생성자 생성 (외부에서 생성 방지)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Game {

    // 경기 고유 번호 (PK)
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

    // 경기 날짜 및 시간
    @Column(nullable = false)
    private LocalDateTime gameDate;

    // 등록 시간
    private LocalDateTime createdAt;

    // Game 객체 생성
    @Builder
    public Game(String homeTeam,
                String awayTeam,
                String stadium,
                LocalDateTime gameDate,
                LocalDateTime createdAt) {

        this.homeTeam = homeTeam;
        this.awayTeam = awayTeam;
        this.stadium = stadium;
        this.gameDate = gameDate;
        this.createdAt = createdAt;
    }
}