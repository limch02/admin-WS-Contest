package com.admincontest.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ReservationController {

    @GetMapping("/reserve")
    public String reserve() {
        return "reserve";
    }

    @GetMapping("/room-time")
    public String roomTime() {
        return "room-time";
    }

    @GetMapping("/reservation-detail")
    public String reservationDetail() {
        return "reservation-detail";
    }
}

