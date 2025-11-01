package com.admincontest.waitlist.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class WaitlistRequest {
    private Long roomId;
    private String date;
    private int startHour;
    private int endHour;
}

