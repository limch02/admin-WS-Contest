package com.admincontest.reservation.domain;

import com.admincontest.classroom.domain.Classroom;
import com.admincontest.user.domain.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "Reservation")
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "reservation_id")
    private Long id;

    @Column(name = "reservation_started_at", nullable = false)
    private LocalDateTime reservationStartedAt;

    @Column(name = "reservation_ended_at", nullable = false)
    private LocalDateTime reservationEndedAt;

    @Column(name = "reservation_num")
    private Integer reservationNum;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false)
    private Classroom room;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "status", length = 20)
    private String status;

    @Builder
    private Reservation(LocalDateTime reservationStartedAt, LocalDateTime reservationEndedAt, 
                       Integer reservationNum, Classroom room, User user, String status) {
        this.reservationStartedAt = reservationStartedAt;
        this.reservationEndedAt = reservationEndedAt;
        this.reservationNum = reservationNum;
        this.room = room;
        this.user = user;
        this.status = status != null ? status : "ACTIVE";
    }

    public static Reservation of(LocalDateTime reservationStartedAt, LocalDateTime reservationEndedAt,
                                 Classroom room, User user) {
        return Reservation.builder()
                .reservationStartedAt(reservationStartedAt)
                .reservationEndedAt(reservationEndedAt)
                .reservationNum(0)
                .room(room)
                .user(user)
                .status("ACTIVE")
                .build();
    }
}

