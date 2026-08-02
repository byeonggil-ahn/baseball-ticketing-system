package com.ticket.baseball.reservation.entity;

import com.ticket.baseball.game.entity.Game;
import com.ticket.baseball.seat.entity.Seat;
import com.ticket.baseball.user.entity.User;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "reservations")
@Getter
@NoArgsConstructor
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 예약한 사용자
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    // 예약한 경기
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "game_id")
    private Game game;

    // 예약한 좌석
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seat_id")
    private Seat seat;

    // 예약 시간
    private LocalDateTime reservedAt;

    @Builder
    public Reservation(User user, Game game, Seat seat) {
        this.user = user;
        this.game = game;
        this.seat = seat;
        this.reservedAt = LocalDateTime.now();
    }
}