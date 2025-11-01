package com.admincontest.waitlist.repository;

import com.admincontest.waitlist.domain.Waitlist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface WaitlistRepository extends JpaRepository<Waitlist, Long> {
    
    // 특정 강의실, 날짜, 시간대의 대기 목록 조회
    @Query("SELECT w FROM Waitlist w WHERE w.room.id = :roomId " +
           "AND w.reservationStartedAt >= :startDate " +
           "AND w.reservationStartedAt < :endDate " +
           "AND w.status = 'WAITING' " +
           "ORDER BY w.queuePosition ASC")
    List<Waitlist> findByRoomIdAndDateRange(@Param("roomId") Long roomId,
                                            @Param("startDate") LocalDateTime startDate,
                                            @Param("endDate") LocalDateTime endDate);
    
    // 특정 사용자의 대기 목록 조회
    @Query("SELECT w FROM Waitlist w WHERE w.user.userId = :userId AND w.status = 'WAITING'")
    List<Waitlist> findByUserIdAndStatusWaiting(@Param("userId") Long userId);
    
    // 특정 사용자의 특정 강의실/시간대 대기 신청 조회
    @Query("SELECT w FROM Waitlist w WHERE w.user.userId = :userId " +
           "AND w.room.id = :roomId " +
           "AND w.reservationStartedAt = :startDateTime " +
           "AND w.reservationEndedAt = :endDateTime " +
           "AND w.status = 'WAITING'")
    Optional<Waitlist> findByUserAndRoomAndTime(@Param("userId") Long userId,
                                               @Param("roomId") Long roomId,
                                               @Param("startDateTime") LocalDateTime startDateTime,
                                               @Param("endDateTime") LocalDateTime endDateTime);
    
    // 특정 강의실/시간대의 최대 큐 순위 조회
    @Query("SELECT COALESCE(MAX(w.queuePosition), 0) FROM Waitlist w WHERE w.room.id = :roomId " +
           "AND w.reservationStartedAt >= :startDate " +
           "AND w.reservationStartedAt < :endDate " +
           "AND w.status = 'WAITING'")
    Long findMaxQueuePosition(@Param("roomId") Long roomId,
                             @Param("startDate") LocalDateTime startDate,
                             @Param("endDate") LocalDateTime endDate);
}

