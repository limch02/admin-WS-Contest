package com.admincontest.reservation.service;

import com.admincontest.classroom.domain.Classroom;
import com.admincontest.classroom.service.ClassroomService;
import com.admincontest.reservation.domain.Reservation;
import com.admincontest.reservation.dto.ReservationRequest;
import com.admincontest.reservation.repository.ReservationRepository;
import com.admincontest.user.domain.User;
import com.admincontest.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final ClassroomService classroomService;
    private final UserRepository userRepository;

    @Transactional
    public Long createReservation(ReservationRequest request) {
        Classroom classroom = classroomService.findClassroom(request.getRoomId());
        User user = userRepository.findByLoginId(request.getRepresentativeId())
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        
        LocalDate date = LocalDate.parse(request.getDate());
        LocalDateTime startDateTime = date.atTime(request.getStartHour(), 0);
        LocalDateTime endDateTime = date.atTime(request.getEndHour(), 0);
        
        LocalDateTime now = LocalDateTime.now();
        
        // 강의실 정원 체크 (대표 예약자 1명 + 멤버 수)
        int totalParticipants = 1 + (request.getMembers() != null ? request.getMembers().size() : 0);
        if (totalParticipants > classroom.getCapacity()) {
            throw new IllegalArgumentException("강의실 정원(" + classroom.getCapacity() + "명)을 초과했습니다. 현재 참여 인원: " + totalParticipants + "명");
        }
        
        // 대표 예약자의 활성화된 미래 예약 개수 제한 (최대 3개)
        List<Reservation> representativeReservations = reservationRepository.findActiveFutureReservationsByUserId(
                user.getUserId(), now);
        
        if (representativeReservations.size() >= 3) {
            throw new IllegalArgumentException("대표 예약자의 활성화된 예약이 3개 이상입니다. 기존 예약을 취소한 후 다시 시도해주세요.");
        }
        
        // 그룹 멤버들의 활성화된 미래 예약 개수 제한 체크
        if (request.getMembers() != null && !request.getMembers().isEmpty()) {
            for (ReservationRequest.Member member : request.getMembers()) {
                User memberUser = userRepository.findByLoginId(member.getStudentId())
                        .orElseThrow(() -> new IllegalArgumentException("참여자 '" + member.getStudentName() + "(" + member.getStudentId() + ")'를 찾을 수 없습니다."));
                
                List<Reservation> memberReservations = reservationRepository.findActiveFutureReservationsByUserId(
                        memberUser.getUserId(), now);
                
                if (memberReservations.size() >= 3) {
                    throw new IllegalArgumentException("참여자 '" + member.getStudentName() + "(" + member.getStudentId() + ")'의 활성화된 예약이 3개 이상입니다. 해당 참여자는 예약에 포함될 수 없습니다.");
                }
            }
        }
        
        // 중복 예약 체크 (같은 강의실, 같은 시간대)
        // 날짜 범위로 조회: 해당 날짜의 시작(00:00:00)부터 다음 날 시작 전까지
        LocalDateTime dateStart = date.atStartOfDay();
        LocalDateTime dateEnd = date.plusDays(1).atStartOfDay();
        List<Reservation> existingReservations = reservationRepository.findByClassroomIdAndDate(
                classroom.getId(), dateStart, dateEnd);
        boolean isOverlapping = existingReservations.stream()
                .anyMatch(r -> {
                    LocalDateTime rStart = r.getReservationStartedAt();
                    LocalDateTime rEnd = r.getReservationEndedAt();
                    // 시간대가 겹치는지 확인
                    return (startDateTime.isBefore(rEnd) && endDateTime.isAfter(rStart)) &&
                           "ACTIVE".equals(r.getStatus());
                });
        
        if (isOverlapping) {
            throw new IllegalArgumentException("해당 시간대에 이미 예약이 있습니다.");
        }
        
        Reservation reservation = Reservation.of(
                startDateTime,
                endDateTime,
                classroom,
                user
        );
        return reservationRepository.save(reservation).getId();
    }

    public List<Integer> getAvailableTimes(Long roomId, LocalDate date) {
        // 특정 강의실과 날짜의 예약 조회
        // 날짜 범위로 조회: 해당 날짜의 시작(00:00:00)부터 다음 날 시작 전까지
        LocalDateTime dateStart = date.atStartOfDay();
        LocalDateTime dateEnd = date.plusDays(1).atStartOfDay();
        List<Reservation> reservations = reservationRepository.findByClassroomIdAndDate(roomId, dateStart, dateEnd);
        
        // 예약된 시간대 추출
        List<Integer> reservedHours = reservations.stream()
                .flatMap(r -> IntStream.range(
                        r.getReservationStartedAt().getHour(),
                        r.getReservationEndedAt().getHour()
                ).boxed())
                .collect(Collectors.toList());
        
        // 6시부터 21시까지 중 예약되지 않은 시간대 반환 (22시는 선택 불가)
        return IntStream.rangeClosed(6, 21)
                .filter(hour -> !reservedHours.contains(hour))
                .boxed()
                .collect(Collectors.toList());
    }

    /**
     * 특정 강의실과 날짜의 예약 목록 조회 (프론트엔드용)
     */
    public List<java.util.Map<String, Object>> getReservationsByRoomAndDate(Long roomId, LocalDate date) {
        // 날짜 범위로 조회: 해당 날짜의 시작(00:00:00)부터 다음 날 시작 전까지
        LocalDateTime dateStart = date.atStartOfDay();
        LocalDateTime dateEnd = date.plusDays(1).atStartOfDay();
        List<Reservation> reservations = reservationRepository.findByClassroomIdAndDate(roomId, dateStart, dateEnd);
        
        return reservations.stream()
                .filter(r -> "ACTIVE".equals(r.getStatus()))
                .map(r -> {
                    java.util.Map<String, Object> map = new java.util.HashMap<>();
                    map.put("roomId", roomId);
                    map.put("date", date.toString());
                    map.put("startHour", r.getReservationStartedAt().getHour());
                    map.put("endHour", r.getReservationEndedAt().getHour());
                    map.put("reservationId", r.getId());
                    return map;
                })
                .collect(Collectors.toList());
    }
    
    /**
     * 특정 사용자의 예약 내역 조회
     */
    public List<java.util.Map<String, Object>> getUserReservations(Long userId) {
        List<Reservation> reservations = reservationRepository.findAllByUserId(userId);
        
        return reservations.stream()
                .map(r -> {
                    java.util.Map<String, Object> map = new java.util.HashMap<>();
                    map.put("reservationId", r.getId());
                    map.put("roomId", r.getRoom().getId());
                    map.put("roomName", r.getRoom().getName());
                    map.put("location", r.getRoom().getLocation());
                    map.put("date", r.getReservationStartedAt().toLocalDate().toString());
                    map.put("startDateTime", r.getReservationStartedAt().toString());
                    map.put("endDateTime", r.getReservationEndedAt().toString());
                    map.put("startHour", r.getReservationStartedAt().getHour());
                    map.put("endHour", r.getReservationEndedAt().getHour());
                    map.put("status", r.getStatus());
                    map.put("canCancel", canCancelReservation(r));
                    return map;
                })
                .collect(Collectors.toList());
    }
    
    /**
     * 강의실별 예약 타임라인 조회 (특정 날짜 범위)
     */
    public List<java.util.Map<String, Object>> getRoomTimeline(Long roomId, LocalDate startDate, LocalDate endDate) {
        // 날짜 범위 내의 모든 예약 조회 (더 효율적인 쿼리 사용)
        LocalDateTime dateStart = startDate.atStartOfDay();
        LocalDateTime dateEnd = endDate.plusDays(1).atStartOfDay();
        List<Reservation> allReservations = reservationRepository.findByClassroomIdAndDate(roomId, dateStart, dateEnd)
                .stream()
                .filter(r -> "ACTIVE".equals(r.getStatus()))
                .sorted((a, b) -> a.getReservationStartedAt().compareTo(b.getReservationStartedAt()))
                .collect(Collectors.toList());
        
        return allReservations.stream()
                .map(r -> {
                    java.util.Map<String, Object> map = new java.util.HashMap<>();
                    map.put("reservationId", r.getId());
                    map.put("date", r.getReservationStartedAt().toLocalDate().toString());
                    map.put("startDateTime", r.getReservationStartedAt().toString());
                    map.put("endDateTime", r.getReservationEndedAt().toString());
                    map.put("startHour", r.getReservationStartedAt().getHour());
                    map.put("endHour", r.getReservationEndedAt().getHour());
                    map.put("userName", r.getUser().getName());
                    map.put("userLoginId", r.getUser().getLoginId());
                    return map;
                })
                .collect(Collectors.toList());
    }
    
    /**
     * 예약 취소
     */
    @Transactional
    public void cancelReservation(Long reservationId, Long userId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new IllegalArgumentException("예약을 찾을 수 없습니다."));
        
        // 본인의 예약인지 확인
        if (!reservation.getUser().getUserId().equals(userId)) {
            throw new IllegalArgumentException("본인의 예약만 취소할 수 있습니다.");
        }
        
        // 이미 취소된 예약인지 확인
        if ("CANCELLED".equals(reservation.getStatus())) {
            throw new IllegalArgumentException("이미 취소된 예약입니다.");
        }
        
        // 예약 시작 10분 전까지만 취소 가능
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime reservationStart = reservation.getReservationStartedAt();
        if (now.isAfter(reservationStart.minusMinutes(10))) {
            throw new IllegalArgumentException("예약 시작 10분 전까지만 취소 가능합니다.");
        }
        
        // 예약 상태를 CANCELLED로 변경
        reservation.cancel();
        reservationRepository.save(reservation);
    }
    
    /**
     * 예약 취소 가능 여부 확인
     */
    private boolean canCancelReservation(Reservation reservation) {
        if (!"ACTIVE".equals(reservation.getStatus())) {
            return false;
        }
        
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime reservationStart = reservation.getReservationStartedAt();
        
        // 예약 시작 10분 전까지만 취소 가능
        return now.isBefore(reservationStart.minusMinutes(10));
    }
}

