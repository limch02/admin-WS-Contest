package com.admincontest.waitlist.service;

import com.admincontest.classroom.domain.Classroom;
import com.admincontest.classroom.service.ClassroomService;
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
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WaitlistService {

    private final WaitlistRepository waitlistRepository;
    private final ClassroomService classroomService;
    private final UserRepository userRepository;

    @Transactional
    public Long createWaitlist(WaitlistRequest request, String loginId) {
        // 사용자 조회
        User user = userRepository.findByLoginId(loginId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        // 강의실 조회
        Classroom room = classroomService.findClassroom(request.getRoomId());

        // 날짜/시간 변환
        LocalDate date = LocalDate.parse(request.getDate());
        LocalDateTime startDateTime = date.atTime(request.getStartHour(), 0);
        LocalDateTime endDateTime = date.atTime(request.getEndHour(), 0);

        // 중복 체크: 같은 사용자가 같은 강의실/정확한 시간에 이미 대기 신청했는지
        boolean alreadyExists = waitlistRepository.findByUserAndRoomAndExactTime(
                user.getUserId(), request.getRoomId(), startDateTime, endDateTime)
                .isPresent();

        if (alreadyExists) {
            throw new IllegalArgumentException("이미 대기 신청한 시간대입니다.");
        }

        // 대기 순위 계산 (같은 강의실/시간의 대기 목록 개수 + 1)
        LocalDateTime dateStart = date.atStartOfDay();
        LocalDateTime dateEnd = date.plusDays(1).atStartOfDay();
        long existingCount = waitlistRepository.countByRoomIdAndDateRange(
                request.getRoomId(), dateStart, dateEnd);
        Long queuePosition = existingCount + 1;

        // 대기 목록 생성
        Waitlist waitlist = Waitlist.of(user, room, startDateTime, endDateTime);
        waitlist.updateQueuePosition(queuePosition);
        
        return waitlistRepository.save(waitlist).getId();
    }

    // 특정 강의실과 날짜의 대기 목록 조회
    public List<Waitlist> getWaitlistByRoomAndDate(Long roomId, LocalDate date) {
        LocalDateTime dateStart = date.atStartOfDay();
        LocalDateTime dateEnd = date.plusDays(1).atStartOfDay();
        return waitlistRepository.findByRoomIdAndDateRange(roomId, dateStart, dateEnd);
    }
}

