package com.admincontest.reservation.controller;

import com.admincontest.reservation.dto.ReservationRequest;
import com.admincontest.reservation.service.ReservationService;
import com.admincontest.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reservations")
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationService reservationService;
    private final UserRepository userRepository;

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
    
    // 사용자 본인의 예약 내역 조회
    @GetMapping("/my")
    public ResponseEntity<?> getMyReservations() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            
            String loginId = authentication.getName();
            Long userId = userRepository.findByLoginId(loginId)
                    .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."))
                    .getUserId();
            
            List<Map<String, Object>> reservations = reservationService.getUserReservations(userId);
            return ResponseEntity.ok(reservations);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "예약 내역 조회 중 오류가 발생했습니다: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    // 예약 취소
    @DeleteMapping("/{reservationId}")
    public ResponseEntity<?> cancelReservation(@PathVariable Long reservationId) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            
            String loginId = authentication.getName();
            Long userId = userRepository.findByLoginId(loginId)
                    .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."))
                    .getUserId();
            
            reservationService.cancelReservation(reservationId, userId);
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "예약이 취소되었습니다.");
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "예약 취소 중 오류가 발생했습니다: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    // 강의실별 예약 타임라인 조회
    @GetMapping("/room/{roomId}/timeline")
    public ResponseEntity<?> getRoomTimeline(
            @PathVariable Long roomId,
            @RequestParam(required = false, defaultValue = "") String startDate,
            @RequestParam(required = false, defaultValue = "") String endDate) {
        try {
            LocalDate start = startDate.isEmpty() ? LocalDate.now() : LocalDate.parse(startDate);
            LocalDate end = endDate.isEmpty() ? LocalDate.now().plusDays(7) : LocalDate.parse(endDate);
            
            List<Map<String, Object>> timeline = reservationService.getRoomTimeline(roomId, start, end);
            return ResponseEntity.ok(timeline);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "타임라인 조회 중 오류가 발생했습니다: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
}

