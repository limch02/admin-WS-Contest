package com.admincontest.reservation.repository;

import com.admincontest.reservation.domain.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    List<Reservation> findByClassroomIdAndDate(Long classroomId, LocalDate date);
}

