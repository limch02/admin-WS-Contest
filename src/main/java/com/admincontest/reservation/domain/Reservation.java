package com.admincontest.reservation.domain;

import com.admincontest.classroom.domain.Classroom;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "reservations")
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "reservation_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "classroom_id", nullable = false)
    private Classroom classroom;

    @Column(name = "student_id", nullable = false)
    private String studentId;

    @Column(name = "student_name", nullable = false)
    private String studentName;

    @Column(name = "reservation_date", nullable = false)
    private LocalDate date;

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    @Builder
    private Reservation(Classroom classroom, String studentId, String studentName, LocalDate date, LocalTime startTime, LocalTime endTime) {
        this.classroom = classroom;
        this.studentId = studentId;
        this.studentName = studentName;
        this.date = date;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public static Reservation of(Classroom classroom, String studentId, String studentName, LocalDate date, LocalTime startTime, LocalTime endTime) {
        return Reservation.builder()
                .classroom(classroom)
                .studentId(studentId)
                .studentName(studentName)
                .date(date)
                .startTime(startTime)
                .endTime(endTime)
                .build();
    }
}

