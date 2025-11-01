package com.admincontest.reservation.controller;

import com.admincontest.reservation.dto.ReservationRequest;
import com.admincontest.reservation.service.ReservationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reservations")
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationService reservationService;

    // 예약 생성
    @PostMapping
    public ResponseEntity<?> createReservation(@RequestBody ReservationRequest request) {
        try {
            Long reservationId = reservationService.createReservation(request);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "예약이 완료되었습니다.");
            response.put("reservationId", reservationId);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "예약 생성 중 오류가 발생했습니다: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    // 특정 날짜와 강의실의 예약 목록 조회
    @GetMapping("/room/{roomId}/date/{date}")
    public ResponseEntity<?> getReservationsByRoomAndDate(
            @PathVariable Long roomId,
            @PathVariable String date) {
        try {
            java.time.LocalDate localDate = java.time.LocalDate.parse(date);
            List<Map<String, Object>> reservations = reservationService.getReservationsByRoomAndDate(roomId, localDate);
            return ResponseEntity.ok(reservations);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "예약 목록 조회 중 오류가 발생했습니다: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
}

