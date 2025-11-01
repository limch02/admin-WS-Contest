package com.admincontest.user.service;

import com.admincontest.security.jwt.JwtTokenProvider;
import com.admincontest.user.domain.User;
import com.admincontest.user.domain.UserStatus;
import com.admincontest.user.dto.JwtResponse;
import com.admincontest.user.dto.LoginRequest;
import com.admincontest.user.dto.RegisterRequest;
import com.admincontest.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
class AuthServiceTest {

    @Autowired
    private AuthService authService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("회원가입 성공")
    void testRegisterSuccess() {
        // given
        RegisterRequest request = new RegisterRequest();
        request.setLoginId("STU001");
        request.setPassword("password123");
        request.setEmail("test@example.com");
        request.setName("홍길동");
        request.setRole("STUDENT");

        // when
        User savedUser = authService.register(request);

        // then
        assertNotNull(savedUser);
        assertNotNull(savedUser.getUserId());
        assertEquals("STU001", savedUser.getLoginId());
        assertEquals("test@example.com", savedUser.getEmail());
        assertEquals("홍길동", savedUser.getName());
        assertEquals(UserStatus.STUDENT, savedUser.getRole());
        assertEquals(0, savedUser.getReservationNum());
        assertTrue(passwordEncoder.matches("password123", savedUser.getPassword()));
    }

    @Test
    @DisplayName("회원가입 - 중복된 로그인 ID 예외 발생")
    void testRegisterDuplicateLoginId() {
        // given - 이미 존재하는 사용자
        User existingUser = User.builder()
                .loginId("STU001")
                .password(passwordEncoder.encode("password123"))
                .email("existing@example.com")
                .name("기존사용자")
                .role(UserStatus.STUDENT)
                .reservationNum(0)
                .build();
        userRepository.save(existingUser);

        RegisterRequest request = new RegisterRequest();
        request.setLoginId("STU001");
        request.setPassword("password123");
        request.setEmail("new@example.com");
        request.setName("신규사용자");
        request.setRole("STUDENT");

        // when & then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> authService.register(request)
        );
        assertEquals("이미 사용 중인 로그인 ID입니다.", exception.getMessage());
    }

    @Test
    @DisplayName("회원가입 - 중복된 이메일 예외 발생")
    void testRegisterDuplicateEmail() {
        // given
        User existingUser = User.builder()
                .loginId("STU001")
                .password(passwordEncoder.encode("password123"))
                .email("test@example.com")
                .name("기존사용자")
                .role(UserStatus.STUDENT)
                .reservationNum(0)
                .build();
        userRepository.save(existingUser);

        RegisterRequest request = new RegisterRequest();
        request.setLoginId("STU002");
        request.setPassword("password123");
        request.setEmail("test@example.com");
        request.setName("신규사용자");
        request.setRole("STUDENT");

        // when & then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> authService.register(request)
        );
        assertEquals("이미 사용 중인 이메일입니다.", exception.getMessage());
    }

    @Test
    @DisplayName("회원가입 - 역할이 null이면 기본값 STUDENT")
    void testRegisterDefaultRole() {
        // given
        RegisterRequest request = new RegisterRequest();
        request.setLoginId("STU001");
        request.setPassword("password123");
        request.setEmail("test@example.com");
        request.setName("홍길동");
        request.setRole(null); // null로 설정

        // when
        User savedUser = authService.register(request);

        // then
        assertEquals(UserStatus.STUDENT, savedUser.getRole());
    }

    @Test
    @DisplayName("로그인 성공 및 JWT 토큰 생성")
    void testLoginSuccess() {
        // given
        User user = User.builder()
                .loginId("STU001")
                .password(passwordEncoder.encode("password123"))
                .email("test@example.com")
                .name("홍길동")
                .role(UserStatus.STUDENT)
                .reservationNum(0)
                .build();
        userRepository.save(user);

        LoginRequest request = new LoginRequest();
        request.setLoginId("STU001");
        request.setPassword("password123");

        // when
        JwtResponse jwtResponse = authService.login(request);

        // then
        assertNotNull(jwtResponse);
        assertNotNull(jwtResponse.getToken());
        assertFalse(jwtResponse.getToken().isEmpty());
        assertEquals("Bearer", jwtResponse.getType());
        assertEquals("STU001", jwtResponse.getLoginId());
        assertEquals("STUDENT", jwtResponse.getRole());

        // 토큰 검증
        assertTrue(jwtTokenProvider.validateToken(jwtResponse.getToken()));
        assertEquals("STU001", jwtTokenProvider.getLoginIdFromToken(jwtResponse.getToken()));
        assertEquals("STUDENT", jwtTokenProvider.getRoleFromToken(jwtResponse.getToken()));
    }

    @Test
    @DisplayName("로그인 실패 - 잘못된 비밀번호")
    void testLoginWrongPassword() {
        // given
        User user = User.builder()
                .loginId("STU001")
                .password(passwordEncoder.encode("password123"))
                .email("test@example.com")
                .name("홍길동")
                .role(UserStatus.STUDENT)
                .reservationNum(0)
                .build();
        userRepository.save(user);

        LoginRequest request = new LoginRequest();
        request.setLoginId("STU001");
        request.setPassword("wrongpassword");

        // when & then
        assertThrows(
                Exception.class,
                () -> authService.login(request)
        );
    }

    @Test
    @DisplayName("로그인 실패 - 존재하지 않는 사용자")
    void testLoginUserNotFound() {
        // given
        LoginRequest request = new LoginRequest();
        request.setLoginId("NOTEXIST");
        request.setPassword("password123");

        // when & then
        assertThrows(
                Exception.class,
                () -> authService.login(request)
        );
    }
}

