package com.admincontest.user.service;

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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;

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

        // 역할 검증 및 기본값 설정
        UserStatus role = (request.getRole() != null && request.getRole().equals("ADMIN"))
                ? UserStatus.ADMIN
                : UserStatus.STUDENT;

        // 사용자 생성
        User user = User.builder()
                .loginId(request.getLoginId())
                .password(passwordEncoder.encode(request.getPassword())) // 비밀번호 암호화
                .email(request.getEmail())
                .name(request.getName())
                .role(role)
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
}