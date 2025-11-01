package com.admincontest.classroom.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ClassroomSearchRequest {
    private String date;           // 날짜 (YYYY-MM-DD)
    private Integer startHour;     // 시작 시간 (6-21)
    private Integer endHour;       // 종료 시간 (7-22)
    private Integer minCapacity;   // 최소 수용인원 (선택)
    private Boolean hasProjector;  // 프로젝터 유무 (선택)
    private Boolean hasWhiteboard; // 화이트보드 유무 (선택)
}

