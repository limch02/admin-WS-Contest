package com.admincontest.reservation.repository;

import com.admincontest.reservation.domain.ReservationParticipant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ReservationParticipantRepository extends JpaRepository<ReservationParticipant, Long> {
    
    // 특정 예약의 참여자 목록 조회
    @Query("SELECT rp FROM ReservationParticipant rp WHERE rp.reservation.id = :reservationId")
    List<ReservationParticipant> findByReservationId(@Param("reservationId") Long reservationId);
    
    // 특정 사용자가 참여한 예약 목록 조회
    @Query("SELECT rp FROM ReservationParticipant rp WHERE rp.student.userId = :userId")
    List<ReservationParticipant> findByUserId(@Param("userId") Long userId);
    
    // 특정 사용자가 특정 예약에 참여했는지 확인
    @Query("SELECT COUNT(rp) > 0 FROM ReservationParticipant rp WHERE rp.student.userId = :userId AND rp.reservation.id = :reservationId")
    boolean existsByUserIdAndReservationId(@Param("userId") Long userId, @Param("reservationId") Long reservationId);
}

