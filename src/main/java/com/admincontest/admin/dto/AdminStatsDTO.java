package com.admincontest.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminStatsDTO {
    // 전체 예약 수
    private Long totalReservations;
    
    // 활성 예약 수
    private Long activeReservations;
    
    // 취소된 예약 수
    private Long cancelledReservations;
    
    // 전체 사용자 수
    private Long totalUsers;
    
    // 학생 수
    private Long studentCount;
    
    // 관리자 수
    private Long adminCount;
    
    // 전체 강의실 수
    private Long totalClassrooms;
    
    // 가장 인기 있는 강의실 Top 5 (강의실 이름, 예약 횟수)
    private List<PopularClassroomDTO> popularClassrooms;
    
    // 일별 예약 통계 (날짜, 예약 수)
    private Map<String, Long> dailyReservationStats;
    
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PopularClassroomDTO {
        private Long roomId;
        private String roomName;
        private Long reservationCount;
    }
}

