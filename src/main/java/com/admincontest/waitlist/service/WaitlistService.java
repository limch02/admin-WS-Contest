package com.admincontest.waitlist.service;

import com.admincontest.classroom.domain.Classroom;
import com.admincontest.classroom.service.ClassroomService;
import com.admincontest.reservation.repository.ReservationRepository;
import com.admincontest.user.domain.User;
import com.admincontest.user.repository.UserRepository;
import com.admincontest.waitlist.domain.Waitlist;
import com.admincontest.waitlist.dto.WaitlistRequest;
import com.admincontest.waitlist.repository.WaitlistRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WaitlistService {
    
    private final WaitlistRepository waitlistRepository;
    private final UserRepository userRepository;
    private final ClassroomService classroomService;
    private final ReservationRepository reservationRepository;
    
    /**
     * 대기 신청 생성
     */
    @Transactional
    public Map<String, Object> createWaitlist(WaitlistRequest request, String loginId) {
        // 사용자 조회
        User user = userRepository.findByLoginId(loginId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        
        // 강의실 조회
        Classroom classroom = classroomService.findClassroom(request.getRoomId());
        
        // 날짜 파싱
        LocalDate date = LocalDate.parse(request.getDate());
        LocalDateTime startDateTime = date.atTime(request.getStartHour(), 0);
        LocalDateTime endDateTime = date.atTime(request.getEndHour(), 0);
        
        // 사용자의 활성화된 미래 예약 개수 확인 (최대 3개 제한)
        LocalDateTime now = LocalDateTime.now();
        List<com.admincontest.reservation.domain.Reservation> activeFutureReservations = 
                reservationRepository.findActiveFutureReservationsByUserId(user.getUserId(), now);
        
        if (activeFutureReservations.size() >= 3) {
            throw new IllegalArgumentException("현재 활성화된 예약이 3개 이상이어서 대기 신청을 할 수 없습니다.");
        }
        
        // 중복 대기 신청 확인
        boolean alreadyExists = waitlistRepository.findByUserAndRoomAndTime(
                user.getUserId(), classroom.getId(), startDateTime, endDateTime
        ).isPresent();
        
        if (alreadyExists) {
            throw new IllegalArgumentException("이미 대기 신청한 시간대입니다.");
        }
        
        // 해당 시간대에 예약이 있는지 확인 (예약이 없으면 대기 신청 불가)
        LocalDateTime dateStart = date.atStartOfDay();
        LocalDateTime dateEnd = date.plusDays(1).atStartOfDay();
        List<com.admincontest.reservation.domain.Reservation> existingReservations = 
                reservationRepository.findByClassroomIdAndDate(classroom.getId(), dateStart, dateEnd);
        
        // 해당 시간대에 예약이 있는지 확인
        boolean hasReservation = existingReservations.stream()
                .anyMatch(r -> {
                    LocalDateTime rStart = r.getReservationStartedAt();
                    LocalDateTime rEnd = r.getReservationEndedAt();
                    return (startDateTime.isBefore(rEnd) && endDateTime.isAfter(rStart)) &&
                           "ACTIVE".equals(r.getStatus());
                });
        
        if (!hasReservation) {
            throw new IllegalArgumentException("해당 시간대에 예약이 없어 대기 신청을 할 수 없습니다.");
        }
        
        // 현재 사용자가 이미 해당 시간대에 예약했는지 확인 (예약한 사용자는 대기 신청 불가)
        boolean userHasReservation = existingReservations.stream()
                .anyMatch(r -> {
                    LocalDateTime rStart = r.getReservationStartedAt();
                    LocalDateTime rEnd = r.getReservationEndedAt();
                    return r.getUser().getUserId().equals(user.getUserId()) &&
                           (startDateTime.isBefore(rEnd) && endDateTime.isAfter(rStart)) &&
                           "ACTIVE".equals(r.getStatus());
                });
        
        if (userHasReservation) {
            throw new IllegalArgumentException("이미 해당 시간대에 예약하셨습니다. 예약한 시간대는 대기 신청할 수 없습니다.");
        }
        
        // 큐 순위 계산
        Long maxQueuePosition = waitlistRepository.findMaxQueuePosition(
                classroom.getId(), dateStart, dateEnd);
        Long queuePosition = maxQueuePosition + 1;
        
        // 대기 신청 생성
        Waitlist waitlist = Waitlist.of(user, classroom, startDateTime, endDateTime);
        waitlist.updateQueuePosition(queuePosition);
        Waitlist saved = waitlistRepository.save(waitlist);
        
        Map<String, Object> response = new HashMap<>();
        response.put("waitlistId", saved.getId());
        response.put("queuePosition", queuePosition);
        response.put("message", "대기 신청이 완료되었습니다.");
        
        return response;
    }
    
    /**
     * 특정 강의실과 날짜의 대기 목록 조회 (프론트엔드용)
     */
    public List<Map<String, Object>> getWaitlistsByRoomAndDate(Long roomId, LocalDate date) {
        LocalDateTime dateStart = date.atStartOfDay();
        LocalDateTime dateEnd = date.plusDays(1).atStartOfDay();
        
        List<Waitlist> waitlists = waitlistRepository.findByRoomIdAndDateRange(
                roomId, dateStart, dateEnd);
        
        return waitlists.stream()
                .map(w -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("waitlistId", w.getId());
                    map.put("roomId", roomId);
                    map.put("date", date.toString());
                    map.put("startHour", w.getReservationStartedAt().getHour());
                    map.put("endHour", w.getReservationEndedAt().getHour());
                    map.put("queuePosition", w.getQueuePosition());
                    map.put("userId", w.getUser().getUserId());
                    map.put("loginId", w.getUser().getLoginId()); // 로그인 ID 추가
                    return map;
                })
                .collect(Collectors.toList());
    }
    
    /**
     * 현재 사용자의 대기 목록 조회
     */
    public List<Map<String, Object>> getUserWaitlists(String loginId) {
        User user = userRepository.findByLoginId(loginId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        
        List<Waitlist> waitlists = waitlistRepository.findByUserIdAndStatusWaiting(user.getUserId());
        
        return waitlists.stream()
                .map(w -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("waitlistId", w.getId());
                    map.put("roomId", w.getRoom().getId());
                    map.put("roomName", w.getRoom().getName());
                    map.put("date", w.getReservationStartedAt().toLocalDate().toString());
                    map.put("startHour", w.getReservationStartedAt().getHour());
                    map.put("endHour", w.getReservationEndedAt().getHour());
                    map.put("queuePosition", w.getQueuePosition());
                    return map;
                })
                .collect(Collectors.toList());
    }
    
    /**
     * 대기 신청 취소
     */
    @Transactional
    public void cancelWaitlist(Long waitlistId, String loginId) {
        User user = userRepository.findByLoginId(loginId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        
        Waitlist waitlist = waitlistRepository.findById(waitlistId)
                .orElseThrow(() -> new IllegalArgumentException("대기 신청을 찾을 수 없습니다."));
        
        // 본인의 대기 신청인지 확인
        if (!waitlist.getUser().getUserId().equals(user.getUserId())) {
            throw new IllegalArgumentException("본인의 대기 신청만 취소할 수 있습니다.");
        }
        
        // 이미 취소된 대기 신청인지 확인
        if ("CANCELLED".equals(waitlist.getStatus())) {
            throw new IllegalArgumentException("이미 취소된 대기 신청입니다.");
        }
        
        // 대기 신청 취소
        waitlist.cancel();
        waitlistRepository.save(waitlist);
    }
    
    /**
     * 예약 취소 시 대기 1순위 자동 할당 처리
     */
    @Transactional
    public void processWaitlistOnReservationCancel(Long roomId, LocalDateTime startDateTime, LocalDateTime endDateTime) {
        LocalDate date = startDateTime.toLocalDate();
        LocalDateTime dateStart = date.atStartOfDay();
        LocalDateTime dateEnd = date.plusDays(1).atStartOfDay();
        
        // 해당 시간대의 대기 목록 조회 (순위 순으로)
        List<Waitlist> waitlists = waitlistRepository.findByRoomIdAndDateRange(roomId, dateStart, dateEnd)
                .stream()
                .filter(w -> "WAITING".equals(w.getStatus()))
                .filter(w -> {
                    // 시간대가 겹치는지 확인
                    return (startDateTime.isBefore(w.getReservationEndedAt()) && 
                           endDateTime.isAfter(w.getReservationStartedAt()));
                })
                .sorted((a, b) -> Long.compare(a.getQueuePosition(), b.getQueuePosition()))
                .collect(Collectors.toList());
        
        if (waitlists.isEmpty()) {
            return; // 대기 목록이 없으면 종료
        }
        
        // 대기 1순위부터 순차적으로 처리
        for (Waitlist waitlist : waitlists) {
            User waitlistUser = waitlist.getUser();
            
            // 대기 신청자의 활성화된 미래 예약 개수 확인
            LocalDateTime now = LocalDateTime.now();
            List<com.admincontest.reservation.domain.Reservation> userReservations = 
                    reservationRepository.findActiveFutureReservationsByUserId(waitlistUser.getUserId(), now);
            
            // 예약이 3개 미만인 경우에만 예약 할당
            if (userReservations.size() < 3) {
                // 예약 생성
                com.admincontest.reservation.domain.Reservation newReservation = 
                        com.admincontest.reservation.domain.Reservation.of(
                                waitlist.getReservationStartedAt(),
                                waitlist.getReservationEndedAt(),
                                waitlist.getRoom(),
                                waitlistUser
                        );
                reservationRepository.save(newReservation);
                
                // 대기 신청을 APPROVED로 변경
                waitlist.approve();
                waitlistRepository.save(waitlist);
                
                // 성공적으로 할당했으므로 종료
                return;
            } else {
                // 예약이 3개 이상이면 해당 대기 신청 삭제하고 다음 순위 확인
                waitlist.cancel();
                waitlistRepository.save(waitlist);
                // 다음 순위로 계속 진행
            }
        }
    }
}

