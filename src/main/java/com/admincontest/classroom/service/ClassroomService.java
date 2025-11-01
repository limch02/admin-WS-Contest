package com.admincontest.classroom.service;

import com.admincontest.classroom.domain.Classroom;
import com.admincontest.classroom.dto.ClassroomCreateDTO;
import com.admincontest.classroom.dto.ClassroomSearchRequest;
import com.admincontest.classroom.dto.ClassroomSearchResult;
import com.admincontest.classroom.dto.ClassroomUpdateDTO;
import com.admincontest.classroom.exception.ClassroomAlreadyExistException;
import com.admincontest.classroom.repository.ClassroomRepository;
import com.admincontest.reservation.domain.Reservation;
import com.admincontest.reservation.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ClassroomService {

    private final ClassroomRepository classroomRepository;
    private final ReservationRepository reservationRepository;

    @Transactional
    public Long createClassroom(ClassroomCreateDTO createDTO) {
        // availableDate를 과거 날짜로 설정하여 모든 날짜에 사용 가능하도록 함
        // (필터링 로직: roomDateStr <= selectedDateStr이므로 과거 날짜를 사용)
        if (createDTO.getAvailableDate() == null) {
            createDTO.setAvailableDate(LocalDate.of(1900, 1, 1)); // 모든 날짜에 사용 가능하도록 과거 날짜 설정
        }
        Classroom classroom = createDTO.toEntity();
        Classroom savedClassroom = classroomRepository.save(classroom);
        return savedClassroom.getId();
    }

    public Classroom findClassroom(Long id) {
        return classroomRepository.findById(id)
                .orElseThrow(() -> new ClassroomAlreadyExistException("해당 강의실이 존재하지 않습니다. id=" + id));
    }

    public List<Classroom> findAllClassrooms() {
        return classroomRepository.findAll();
    }

    @Transactional
    public void updateClassroom(Long id, ClassroomUpdateDTO updateDTO) {
        Classroom classroom = findClassroom(id);
        // availableDate가 없으면 기존 날짜 유지
        LocalDate availableDate = updateDTO.getAvailableDate();
        if (availableDate == null) {
            availableDate = classroom.getAvailableDate();
        }
        classroom.update(
                updateDTO.getName(),
                updateDTO.getLocation(),
                updateDTO.getCapacity(),
                updateDTO.isHasWhiteboard(),
                updateDTO.isHasProjector(),
                availableDate
        );
    }

    @Transactional
    public void deleteClassroom(Long id) {
        classroomRepository.deleteById(id);
    }
    
    /**
     * 빈 강의실 검색 (필터링 포함)
     */
    public List<ClassroomSearchResult> searchAvailableClassrooms(ClassroomSearchRequest request) {
        LocalDate date = LocalDate.parse(request.getDate());
        LocalDateTime startDateTime = date.atTime(request.getStartHour(), 0);
        LocalDateTime endDateTime = date.atTime(request.getEndHour(), 0);
        
        // 날짜 범위 설정 (해당 날짜의 00:00:00부터 다음 날 시작 전까지)
        LocalDateTime dateStart = date.atStartOfDay();
        LocalDateTime dateEnd = date.plusDays(1).atStartOfDay();
        
        // 모든 강의실 조회
        List<Classroom> allClassrooms = classroomRepository.findAll();
        
        return allClassrooms.stream()
                .filter(classroom -> {
                    // 1. 날짜 필터링 (availableDate가 선택된 날짜 이전이거나 같아야 함)
                    if (classroom.getAvailableDate().isAfter(date)) {
                        return false;
                    }
                    
                    // 2. 수용인원 필터링
                    if (request.getMinCapacity() != null && classroom.getCapacity() < request.getMinCapacity()) {
                        return false;
                    }
                    
                    // 3. 프로젝터 필터링
                    if (request.getHasProjector() != null && request.getHasProjector() && !classroom.isHasProjector()) {
                        return false;
                    }
                    
                    // 4. 화이트보드 필터링
                    if (request.getHasWhiteboard() != null && request.getHasWhiteboard() && !classroom.isHasWhiteboard()) {
                        return false;
                    }
                    
                    return true;
                })
                .map(classroom -> {
                    // 해당 시간대에 예약이 있는지 확인
                    List<Reservation> reservations = reservationRepository.findByClassroomIdAndDate(
                            classroom.getId(), dateStart, dateEnd);
                    
                    boolean isAvailable = reservations.stream()
                            .noneMatch(r -> {
                                LocalDateTime rStart = r.getReservationStartedAt();
                                LocalDateTime rEnd = r.getReservationEndedAt();
                                // 시간대가 겹치지 않는지 확인 (겹치면 예약 불가)
                                return (startDateTime.isBefore(rEnd) && endDateTime.isAfter(rStart)) &&
                                       "ACTIVE".equals(r.getStatus());
                            });
                    
                    return ClassroomSearchResult.builder()
                            .id(classroom.getId())
                            .name(classroom.getName())
                            .location(classroom.getLocation())
                            .capacity(classroom.getCapacity())
                            .hasProjector(classroom.isHasProjector())
                            .hasWhiteboard(classroom.isHasWhiteboard())
                            .isAvailable(isAvailable)
                            .build();
                })
                .collect(Collectors.toList());
    }
}
