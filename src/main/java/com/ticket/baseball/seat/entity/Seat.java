package com.ticket.baseball.seat.entity;

import com.ticket.baseball.game.entity.Game;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

// 좌석 엔티티
@Getter
@Entity
@NoArgsConstructor
@Table(name = "seats")
public class Seat {

    // 좌석 ID
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 경기 정보
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "game_id", nullable = false)
    private Game game;

    // 좌석 구역 (예: 1루, 3루, 외야)
    @Column(nullable = false)
    private String section;

    // 좌석 행
    @Column(name = "seat_row", nullable = false)
    private Integer rowNumber;

    // 좌석 번호
    @Column(name = "seat_number", nullable = false)
    private Integer seatNumber;

    // 좌석 상태
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SeatStatus status;


    // 낙관적 락 버전
    @Version
    private Long version;

    // 좌석 상태 변경
    public void reserve() {
        this.status = SeatStatus.RESERVED;
    }

    @Builder
    public Seat(Game game,
                String section,
                Integer rowNumber,
                Integer seatNumber,
                SeatStatus status) {
        this.game = game;
        this.section = section;
        this.rowNumber = rowNumber;
        this.seatNumber = seatNumber;
        this.status = status;
    }
}