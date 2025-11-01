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
        
        // 6시부터 22시까지 중 예약되지 않은 시간대 반환
        return IntStream.rangeClosed(6, 22)
                .filter(hour -> !reservedHours.contains(hour))
                .boxed()
                .collect(Collectors.toList());
    }
}

