package com.admincontest.reservation.service;

import com.admincontest.classroom.domain.Classroom;
import com.admincontest.classroom.service.ClassroomService;
import com.admincontest.reservation.domain.Reservation;
import com.admincontest.reservation.dto.ReservationRequest;
import com.admincontest.reservation.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final ClassroomService classroomService;

    @Transactional
    public Long createReservation(ReservationRequest request) {
        Classroom classroom = classroomService.findClassroom(request.getRoomId());
        Reservation reservation = Reservation.of(
                classroom,
                request.getRepresentativeId(),
                request.getRepresentativeName(),
                LocalDate.parse(request.getDate()),
                LocalTime.of(request.getStartHour(), 0),
                LocalTime.of(request.getEndHour(), 0)
        );
        return reservationRepository.save(reservation).getId();
    }

    public List<Integer> getAvailableTimes(Long roomId, LocalDate date) {
        List<Reservation> reservations = reservationRepository.findByClassroomIdAndDate(roomId, date);
        List<Integer> reservedHours = reservations.stream()
                .flatMap(r -> IntStream.range(r.getStartTime().getHour(), r.getEndTime().getHour()).boxed())
                .collect(Collectors.toList());

        return IntStream.rangeClosed(6, 22)
                .filter(hour -> !reservedHours.contains(hour))
                .boxed()
                .collect(Collectors.toList());
    }
}

