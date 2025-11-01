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
        // TODO: 새 엔티티 구조에 맞게 구현 필요
        // 현재는 임시로 모든 시간 반환
        return IntStream.rangeClosed(6, 22)
                .boxed()
                .collect(Collectors.toList());
    }
}

