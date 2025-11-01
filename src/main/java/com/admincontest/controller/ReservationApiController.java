package com.admincontest.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api")
public class ReservationApiController {

    // 강의실 목록 조회
    @GetMapping("/classrooms")
    public ResponseEntity<List<Map<String, Object>>> getClassrooms(@RequestParam(required = false) String date) {
        // TODO: 실제 데이터베이스에서 강의실 목록 조회
        List<Map<String, Object>> classrooms = new ArrayList<>();
        
        Map<String, Object> room1 = new HashMap<>();
        room1.put("id", 1);
        room1.put("name", "[1F] 그룹스터디 01");
        room1.put("capacity", 10);
        room1.put("minPeople", 7);
        room1.put("floor", "1F");
        classrooms.add(room1);
        
        Map<String, Object> room2 = new HashMap<>();
        room2.put("id", 2);
        room2.put("name", "[1F] 그룹스터디 02");
        room2.put("capacity", 10);
        room2.put("minPeople", 7);
        room2.put("floor", "1F");
        classrooms.add(room2);
        
        Map<String, Object> room3 = new HashMap<>();
        room3.put("id", 3);
        room3.put("name", "[1F] 그룹스터디 03");
        room3.put("capacity", 10);
        room3.put("minPeople", 7);
        room3.put("floor", "1F");
        classrooms.add(room3);
        
        Map<String, Object> room4 = new HashMap<>();
        room4.put("id", 4);
        room4.put("name", "[1F] 그룹스터디 04");
        room4.put("capacity", 10);
        room4.put("minPeople", 7);
        room4.put("floor", "1F");
        classrooms.add(room4);
        
        Map<String, Object> room5 = new HashMap<>();
        room5.put("id", 5);
        room5.put("name", "[1F] 그룹스터디 05");
        room5.put("capacity", 6);
        room5.put("minPeople", 4);
        room5.put("floor", "1F");
        classrooms.add(room5);
        
        return ResponseEntity.ok(classrooms);
    }

    // 학생 확인 (학번과 이름으로)
    @PostMapping("/students/check")
    public ResponseEntity<Map<String, Object>> checkStudent(@RequestBody Map<String, String> request) {
        String studentId = request.get("studentId");
        String studentName = request.get("studentName");
        
        // TODO: 실제 데이터베이스에서 학생 정보 확인
        // 현재는 임시로 학번이 8자리 이상이고 이름이 있으면 존재한다고 가정
        boolean exists = studentId != null && studentId.length() >= 8 
                      && studentName != null && studentName.length() > 0;
        
        Map<String, Object> response = new HashMap<>();
        response.put("exists", exists);
        
        if (exists) {
            // TODO: 실제 데이터베이스에서 학생 정보 조회
            Map<String, Object> studentInfo = new HashMap<>();
            studentInfo.put("studentId", studentId);
            studentInfo.put("studentName", studentName);
            response.put("student", studentInfo);
        } else {
            response.put("message", "데이터베이스에 존재하지 않는 사용자입니다.");
        }
        
        return ResponseEntity.ok(response);
    }

    // 예약 완료
    @PostMapping("/reservations")
    public ResponseEntity<Map<String, Object>> createReservation(@RequestBody Map<String, Object> request) {
        // TODO: 실제 데이터베이스에 예약 정보 저장
        // TODO: 등록된 모든 인원에게 알림 발송
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "예약이 완료되었습니다.");
        response.put("reservationId", UUID.randomUUID().toString()); // 임시 예약 ID
        
        // TODO: 알림 발송 로직 추가
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> members = (List<Map<String, Object>>) request.get("members");
        if (members != null) {
            response.put("notificationSent", members.size() + 1); // 대표 예약자 + 참여 인원
        }
        
        return ResponseEntity.ok(response);
    }

    // 날짜별 예약 가능한 시간대 조회
    @GetMapping("/classrooms/{roomId}/available-times")
    public ResponseEntity<List<Integer>> getAvailableTimes(
            @PathVariable Long roomId,
            @RequestParam String date) {
        // TODO: 실제 데이터베이스에서 해당 날짜와 강의실의 예약 현황 조회
        // 현재는 모든 시간대를 사용 가능하다고 가정 (06시 ~ 22시)
        List<Integer> availableTimes = new ArrayList<>();
        for (int hour = 6; hour <= 22; hour++) {
            availableTimes.add(hour);
        }
        
        return ResponseEntity.ok(availableTimes);
    }
}

