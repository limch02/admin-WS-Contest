package com.admincontest.user.service;

import com.admincontest.reservation.domain.Reservation;
import com.admincontest.reservation.repository.ReservationRepository;
import com.admincontest.security.jwt.JwtTokenProvider;
import com.admincontest.user.domain.User;
import com.admincontest.user.domain.UserStatus;
import com.admincontest.user.dto.LoginRequest;
import com.admincontest.user.dto.RegisterRequest;
import com.admincontest.user.dto.JwtResponse;
import com.admincontest.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;
    private final ReservationRepository reservationRepository;

    // 회원가입
    @Transactional
    public User register(RegisterRequest request) {
        // 중복 확인
        if (userRepository.existsByLoginId(request.getLoginId())) {
            throw new IllegalArgumentException("이미 사용 중인 로그인 ID입니다.");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
        }

        // 회원가입은 항상 학생(STUDENT)만 가능
        // 관리자 계정은 애플리케이션 시작 시 자동 생성됨 (adminid/adminpw)

        // 사용자 생성
        User user = User.builder()
                .loginId(request.getLoginId())
                .password(passwordEncoder.encode(request.getPassword())) // 비밀번호 암호화
                .email(request.getEmail())
                .name(request.getName())
                .role(UserStatus.STUDENT) // 항상 학생으로 설정
                .reservationNum(0)
                .build();

        return userRepository.save(user);
    }

    // 로그인 (JWT 토큰 반환)
    public JwtResponse login(LoginRequest request) {
        // 인증 시도
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getLoginId(),
                        request.getPassword()
                )
        );

        // 사용자 정보 조회
        User user = userRepository.findByLoginId(request.getLoginId())
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        // JWT 토큰 생성
        String token = jwtTokenProvider.generateToken(user.getLoginId(), user.getRole().name());

        // JWT 응답 객체 생성 및 반환
        return new JwtResponse(token, user.getLoginId(), user.getRole().name());
    }

    // 학번으로 사용자 검색 (예약 참여자 등록용)
    @Transactional(readOnly = true)
    public ResponseEntity<?> findUserByLoginId(String loginId) {
        User user = userRepository.findByLoginId(loginId)
                .orElseThrow(() -> new IllegalArgumentException("해당 학번의 사용자를 찾을 수 없습니다."));

        // 학생(STUDENT)만 등록 가능
        if (user.getRole() != UserStatus.STUDENT) {
            throw new IllegalArgumentException("학생만 예약 참여자로 등록할 수 있습니다.");
        }

        // 사용자의 활성화된 미래 예약 개수 조회
        LocalDateTime now = LocalDateTime.now();
        List<Reservation> activeFutureReservations = reservationRepository.findActiveFutureReservationsByUserId(
                user.getUserId(), now);
        int reservationCount = activeFutureReservations.size();

        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("loginId", user.getLoginId());
        userInfo.put("name", user.getName());
        userInfo.put("email", user.getEmail());
        userInfo.put("reservationCount", reservationCount);
        userInfo.put("canParticipate", reservationCount < 3);

        return ResponseEntity.ok(userInfo);
    }

    // 사용자의 활성화된 미래 예약 개수 조회
    @Transactional(readOnly = true)
    public ResponseEntity<?> getUserReservationCount(String loginId) {
        User user = userRepository.findByLoginId(loginId)
                .orElseThrow(() -> new IllegalArgumentException("해당 학번의 사용자를 찾을 수 없습니다."));

        LocalDateTime now = LocalDateTime.now();
        List<Reservation> activeFutureReservations = reservationRepository.findActiveFutureReservationsByUserId(
                user.getUserId(), now);
        int count = activeFutureReservations.size();

        Map<String, Object> response = new HashMap<>();
        response.put("loginId", user.getLoginId());
        response.put("name", user.getName());
        response.put("reservationCount", count);
        response.put("canParticipate", count < 3);

        return ResponseEntity.ok(response);
    }
}