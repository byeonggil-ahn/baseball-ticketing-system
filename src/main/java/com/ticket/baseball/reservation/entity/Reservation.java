package com.ticket.baseball.reservation.entity;

import com.ticket.baseball.game.entity.Game;
import com.ticket.baseball.user.entity.User;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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

    // 예약 좌석 목록
    @OneToMany(
            mappedBy = "reservation",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<ReservationSeat> reservationSeats = new ArrayList<>();

    // 예약 시간
    private LocalDateTime reservedAt;

    // 예약 상태
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReservationStatus status;

    @Builder
    public Reservation(
            User user,
            Game game
    ) {
        this.user = user;
        this.game = game;
        this.reservedAt = LocalDateTime.now();
        this.status = ReservationStatus.RESERVED;
    }

    // 예약 좌석 추가
    public void addReservationSeat(
            ReservationSeat reservationSeat
    ) {
        this.reservationSeats.add(reservationSeat);
    }

    // 예약 취소
    public void cancel() {
        this.status = ReservationStatus.CANCELLED;
    }
}