package com.admincontest.waitlist.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class WaitlistRequest {
    private Long roomId;
    private String date;  // "YYYY-MM-DD"
    private int startHour;
    private int endHour;
}

