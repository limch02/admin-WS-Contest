package com.admincontest.classroom.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClassroomSearchResult {
    private Long id;
    private String name;
    private String location;
    private Integer capacity;
    private Boolean hasProjector;
    private Boolean hasWhiteboard;
    private Boolean isAvailable;  // 해당 시간에 예약 가능한지 여부
}

