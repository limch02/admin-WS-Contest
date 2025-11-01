package com.admincontest.reservation.repository;

import com.admincontest.reservation.domain.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    
    // 예약 상태별 카운트
    @Query("SELECT COUNT(r) FROM Reservation r")
    long countAll();
    
    // 강의실별 예약 수 카운트
    @Query("SELECT COUNT(r) FROM Reservation r WHERE r.room.id = :roomId")
    long countByRoomId(@Param("roomId") Long roomId);
    
    // 날짜 기준으로 조회 (reservationStartedAt 필드 사용)
    @Query("SELECT r FROM Reservation r WHERE r.reservationStartedAt >= :date")
    List<Reservation> findAllByDateAfter(@Param("date") LocalDateTime date);
}

