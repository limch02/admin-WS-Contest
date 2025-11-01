package com.admincontest.reservation.dto;

import lombok.Getter;

import java.util.List;

@Getter
public class ReservationRequest {
    private Long roomId;
    private String date;
    private int startHour;
    private int endHour;
    private String representativeId;
    private String representativeName;
    private List<Member> members;

    @Getter
    public static class Member {
        private String studentId;
        private String studentName;
    }
}

