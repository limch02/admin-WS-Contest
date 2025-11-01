package com.admincontest.waitlist.controller;

import com.admincontest.waitlist.dto.WaitlistRequest;
import com.admincontest.waitlist.service.WaitlistService;
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
@RequestMapping("/api/waitlist")
@RequiredArgsConstructor
public class WaitlistController {
    
    private final WaitlistService waitlistService;
    
    /**
     * 대기 신청 생성
     */
    @PostMapping
    public ResponseEntity<?> createWaitlist(@RequestBody WaitlistRequest request) {
        try {
            // 현재 로그인한 사용자 정보 가져오기
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "인증이 필요합니다.");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }
            
            String loginId = authentication.getName();
            Map<String, Object> response = waitlistService.createWaitlist(request, loginId);
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
    
    /**
     * 특정 강의실과 날짜의 대기 목록 조회 (인증 불필요)
     */
    @GetMapping("/room/{roomId}/date/{date}")
    public ResponseEntity<?> getWaitlistsByRoomAndDate(
            @PathVariable Long roomId,
            @PathVariable String date) {
        try {
            LocalDate localDate = LocalDate.parse(date);
            List<Map<String, Object>> waitlists = waitlistService.getWaitlistsByRoomAndDate(roomId, localDate);
            return ResponseEntity.ok(waitlists);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "대기 목록 조회 중 오류가 발생했습니다: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    /**
     * 현재 사용자의 대기 목록 조회
     */
    @GetMapping("/my")
    public ResponseEntity<?> getMyWaitlists() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "인증이 필요합니다.");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }
            
            String loginId = authentication.getName();
            List<Map<String, Object>> waitlists = waitlistService.getUserWaitlists(loginId);
            return ResponseEntity.ok(waitlists);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "대기 목록 조회 중 오류가 발생했습니다: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
}

