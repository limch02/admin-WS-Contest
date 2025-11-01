package com.admincontest.reservation.domain;

import com.admincontest.user.domain.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "Reserve_User")
public class ReservationParticipant {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "reservation_participant_id")
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private User student;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reservation_id", nullable = false)
    private Reservation reservation;
    
    @Builder
    private ReservationParticipant(User student, Reservation reservation) {
        this.student = student;
        this.reservation = reservation;
    }
    
    public static ReservationParticipant of(User student, Reservation reservation) {
        return ReservationParticipant.builder()
                .student(student)
                .reservation(reservation)
                .build();
    }
}

