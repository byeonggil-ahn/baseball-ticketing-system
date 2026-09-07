package com.ticket.baseball.reservation.entity;

import com.ticket.baseball.seat.entity.Seat;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name = "reservation_seat",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_reservation_seat",
                        columnNames = {"reservation_id", "seat_id"}
                )
        }
)
@Getter
@NoArgsConstructor
public class ReservationSeat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 예약
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reservation_id", nullable = false)
    private Reservation reservation;

    // 좌석
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seat_id", nullable = false)
    private Seat seat;

    @Builder
    public ReservationSeat(
            Reservation reservation,
            Seat seat
    ) {
        this.reservation = reservation;
        this.seat = seat;
    }
}