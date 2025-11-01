package com.admincontest.waitlist.repository;

import com.admincontest.waitlist.domain.Waitlist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface WaitlistRepository extends JpaRepository<Waitlist, Long> {
    
    // 특정 강의실과 날짜/시간의 대기 목록 조회
    @Query("SELECT w FROM Waitlist w WHERE w.room.id = :roomId " +
           "AND w.reservationStartedAt >= :startDate " +
           "AND w.reservationStartedAt < :endDate " +
           "AND w.status = 'WAITING' " +
           "ORDER BY w.queuePosition ASC")
    List<Waitlist> findByRoomIdAndDateRange(@Param("roomId") Long roomId,
                                             @Param("startDate") LocalDateTime startDate,
                                             @Param("endDate") LocalDateTime endDate);
    
    // 특정 사용자의 대기 목록 조회
    @Query("SELECT w FROM Waitlist w WHERE w.user.userId = :userId " +
           "AND w.status = 'WAITING'")
    List<Waitlist> findByUserId(@Param("userId") Long userId);
    
    // 특정 강의실/날짜/시간의 대기 목록 개수
    @Query("SELECT COUNT(w) FROM Waitlist w WHERE w.room.id = :roomId " +
           "AND w.reservationStartedAt >= :startDate " +
           "AND w.reservationStartedAt < :endDate " +
           "AND w.status = 'WAITING'")
    long countByRoomIdAndDateRange(@Param("roomId") Long roomId,
                                    @Param("startDate") LocalDateTime startDate,
                                    @Param("endDate") LocalDateTime endDate);
    
    // 중복 체크: 같은 사용자가 같은 강의실/시간에 이미 대기 신청했는지
    @Query("SELECT w FROM Waitlist w WHERE w.user.userId = :userId " +
           "AND w.room.id = :roomId " +
           "AND w.reservationStartedAt >= :startDate " +
           "AND w.reservationStartedAt < :endDate " +
           "AND w.status = 'WAITING'")
    Optional<Waitlist> findByUserAndRoomAndDate(@Param("userId") Long userId,
                                                 @Param("roomId") Long roomId,
                                                 @Param("startDate") LocalDateTime startDate,
                                                 @Param("endDate") LocalDateTime endDate);
    
    // 특정 사용자와 강의실/날짜/시간의 대기 목록 조회 (정확한 시간대 매칭)
    @Query("SELECT w FROM Waitlist w WHERE w.user.userId = :userId " +
           "AND w.room.id = :roomId " +
           "AND w.reservationStartedAt = :reservationStart " +
           "AND w.reservationEndedAt = :reservationEnd " +
           "AND w.status = 'WAITING'")
    Optional<Waitlist> findByUserAndRoomAndExactTime(@Param("userId") Long userId,
                                                     @Param("roomId") Long roomId,
                                                     @Param("reservationStart") LocalDateTime reservationStart,
                                                     @Param("reservationEnd") LocalDateTime reservationEnd);
}

