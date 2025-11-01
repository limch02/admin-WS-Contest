package com.admincontest.admin.service;

import com.admincontest.admin.dto.AdminStatsDTO;
import com.admincontest.admin.dto.UserManagementDTO;
import com.admincontest.classroom.domain.Classroom;
import com.admincontest.classroom.repository.ClassroomRepository;
import com.admincontest.reservation.domain.Reservation;
import com.admincontest.reservation.repository.ReservationRepository;
import com.admincontest.user.domain.User;
import com.admincontest.user.domain.UserStatus;
import com.admincontest.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminService {

    private final UserRepository userRepository;
    private final ClassroomRepository classroomRepository;
    private final ReservationRepository reservationRepository;

    /**
     * 관리자 대시보드 통계 조회
     */
    public AdminStatsDTO getAdminStats() {
        // 예약 통계
        Long totalReservations = reservationRepository.count();
        // 상태별 예약 수 계산
        Long activeReservations = reservationRepository.findAll().stream()
                .filter(r -> "ACTIVE".equals(r.getStatus()))
                .count();
        Long cancelledReservations = reservationRepository.findAll().stream()
                .filter(r -> "CANCELLED".equals(r.getStatus()))
                .count();

        // 사용자 통계
        Long totalUsers = userRepository.count();
        Long studentCount = userRepository.countByRole(UserStatus.STUDENT);
        Long adminCount = userRepository.countByRole(UserStatus.ADMIN);

        // 강의실 통계
        Long totalClassrooms = classroomRepository.count();

        // 인기 강의실 Top 5
        List<AdminStatsDTO.PopularClassroomDTO> popularClassrooms = getPopularClassrooms();

        // 일별 예약 통계 (최근 7일)
        Map<String, Long> dailyStats = getDailyReservationStats();

        return AdminStatsDTO.builder()
                .totalReservations(totalReservations)
                .activeReservations(activeReservations)
                .cancelledReservations(cancelledReservations)
                .totalUsers(totalUsers)
                .studentCount(studentCount)
                .adminCount(adminCount)
                .totalClassrooms(totalClassrooms)
                .popularClassrooms(popularClassrooms)
                .dailyReservationStats(dailyStats)
                .build();
    }

    /**
     * 전체 사용자 목록 조회
     */
    public List<UserManagementDTO> getAllUsers() {
        return userRepository.findAll().stream()
                .map(user -> UserManagementDTO.builder()
                        .userId(user.getUserId())
                        .loginId(user.getLoginId())
                        .name(user.getName())
                        .email(user.getEmail())
                        .role(user.getRole().name())
                        .reservationNum(user.getReservationNum())
                        .createdAt(user.getCreatedAt())
                        .build())
                .collect(Collectors.toList());
    }

    /**
     * 사용자 역할 변경
     */
    @Transactional
    public void updateUserRole(Long userId, String role) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        try {
            UserStatus newRole = UserStatus.valueOf(role.toUpperCase());
            user.setRole(newRole);
            userRepository.save(user);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("유효하지 않은 역할입니다: " + role);
        }
    }

    /**
     * 사용자 삭제
     */
    @Transactional
    public void deleteUser(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new IllegalArgumentException("사용자를 찾을 수 없습니다.");
        }
        userRepository.deleteById(userId);
    }

    /**
     * 인기 강의실 Top 5 조회
     */
    private List<AdminStatsDTO.PopularClassroomDTO> getPopularClassrooms() {
        List<Classroom> classrooms = classroomRepository.findAll();
        
        return classrooms.stream()
                .map(classroom -> {
                    Long reservationCount = reservationRepository.countByRoomId(classroom.getId());
                    return AdminStatsDTO.PopularClassroomDTO.builder()
                            .roomId(classroom.getId())
                            .roomName(classroom.getName())
                            .reservationCount(reservationCount)
                            .build();
                })
                .sorted((a, b) -> Long.compare(b.getReservationCount(), a.getReservationCount()))
                .limit(5)
                .collect(Collectors.toList());
    }

    /**
     * 일별 예약 통계 (최근 7일)
     */
    private Map<String, Long> getDailyReservationStats() {
        LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);
        List<Reservation> recentReservations = reservationRepository
                .findAllByDateAfter(sevenDaysAgo);

        return recentReservations.stream()
                .collect(Collectors.groupingBy(
                        reservation -> reservation.getReservationStartedAt().toLocalDate().toString(),
                        Collectors.counting()
                ));
    }
}

