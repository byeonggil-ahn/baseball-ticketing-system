package com.ticket.baseball.seat.entity;

import com.ticket.baseball.game.entity.Game;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "seats")
public class Seat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 좌석이 속한 경기
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "game_id", nullable = false)
    private Game game;

    // 좌석 구역
    @Column(nullable = false)
    private String section;

    // 좌석 번호
    @Column(name = "seat_number", nullable = false)
    private Integer seatNumber;

    // 좌석 등급
    @Column(name = "seat_grade", nullable = false, length = 20)
    private String seatGrade;

    // 좌석 가격
    @Column(nullable = false)
    private Integer price;

    // 좌석 상태
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SeatStatus status;

    // 동시성 제어 버전
    @Version
    @Column(nullable = false)
    private Long version;

    // 생성 시간
    @Column(nullable = false)
    private LocalDateTime createdAt;

    // 좌석 예약
    public void reserve() {
        this.status = SeatStatus.RESERVED;
    }

    // 예약 취소
    public void cancelReservation() {
        this.status = SeatStatus.AVAILABLE;
    }

    @Builder
    public Seat(Game game,
                String section,
                Integer seatNumber,
                String seatGrade,
                Integer price,
                SeatStatus status,
                LocalDateTime createdAt) {

        this.game = game;
        this.section = section;
        this.seatNumber = seatNumber;
        this.seatGrade = seatGrade;
        this.price = price;
        this.status = status;
        this.createdAt = createdAt;
    }
}