package com.admincontest.waitlist.controller;

import com.admincontest.waitlist.dto.WaitlistRequest;
import com.admincontest.waitlist.service.WaitlistService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/waitlist")
@RequiredArgsConstructor
public class WaitlistController {

    private final WaitlistService waitlistService;

    // 대기 신청 생성
    @PostMapping
    public ResponseEntity<?> createWaitlist(@RequestBody WaitlistRequest request) {
        try {
            // 현재 로그인한 사용자 정보 가져오기
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "로그인이 필요합니다.");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }

            String loginId = authentication.getName();
            Long waitlistId = waitlistService.createWaitlist(request, loginId);
            
            // 대기 순위 계산
            java.time.LocalDate date = java.time.LocalDate.parse(request.getDate());
            java.util.List<com.admincontest.waitlist.domain.Waitlist> waitlists = 
                waitlistService.getWaitlistByRoomAndDate(request.getRoomId(), date);
            long queuePosition = waitlists.stream()
                .filter(w -> w.getId().equals(waitlistId))
                .mapToLong(w -> w.getQueuePosition())
                .findFirst()
                .orElse(1L);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "대기 신청이 완료되었습니다.");
            response.put("waitlistId", waitlistId);
            response.put("queuePosition", queuePosition);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "대기 신청 중 오류가 발생했습니다: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    // 특정 강의실과 날짜의 대기 목록 조회
    @GetMapping("/room/{roomId}/date/{date}")
    public ResponseEntity<?> getWaitlistByRoomAndDate(
            @PathVariable Long roomId,
            @PathVariable String date) {
        try {
            java.time.LocalDate localDate = java.time.LocalDate.parse(date);
            java.util.List<com.admincontest.waitlist.domain.Waitlist> waitlists = 
                waitlistService.getWaitlistByRoomAndDate(roomId, localDate);
            
            java.util.List<Map<String, Object>> result = waitlists.stream()
                .map(w -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("roomId", roomId);
                    map.put("date", date);
                    map.put("startHour", w.getReservationStartedAt().getHour());
                    map.put("endHour", w.getReservationEndedAt().getHour());
                    map.put("queuePosition", w.getQueuePosition());
                    map.put("waitlistId", w.getId());
                    return map;
                })
                .collect(java.util.stream.Collectors.toList());
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "대기 목록 조회 중 오류가 발생했습니다: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
}

