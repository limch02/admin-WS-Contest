package com.admincontest.user.integration;

import com.admincontest.user.domain.User;
import com.admincontest.user.domain.UserStatus;
import com.admincontest.user.dto.JwtResponse;
import com.admincontest.user.dto.LoginRequest;
import com.admincontest.user.dto.RegisterRequest;
import com.admincontest.user.repository.UserRepository;
import com.admincontest.user.service.AuthService;
import com.admincontest.security.jwt.JwtTokenProvider;
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
@DisplayName("인증 DB 통합 테스트")
class AuthIntegrationTest {

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
        // 테스트 전 데이터베이스 초기화
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("DB 연결 확인 - 회원가입 후 DB에 저장 확인")
    void testDatabaseConnection_Register() {
        // given
        RegisterRequest request = new RegisterRequest();
        request.setLoginId("DBTEST001");
        request.setPassword("password123");
        request.setEmail("dbtest@example.com");
        request.setName("DB테스트");
        request.setRole("STUDENT");

        // when - 회원가입
        User savedUser = authService.register(request);

        // then - DB에 저장되었는지 확인
        assertNotNull(savedUser.getUserId(), "사용자 ID가 생성되어야 합니다");
        
        // DB에서 직접 조회하여 확인
        User foundUser = userRepository.findByLoginId("DBTEST001")
                .orElseThrow(() -> new AssertionError("DB에서 사용자를 찾을 수 없습니다"));
        
        assertEquals("DBTEST001", foundUser.getLoginId(), "로그인 ID가 일치해야 합니다");
        assertEquals("dbtest@example.com", foundUser.getEmail(), "이메일이 일치해야 합니다");
        assertEquals("DB테스트", foundUser.getName(), "이름이 일치해야 합니다");
        assertEquals(UserStatus.STUDENT, foundUser.getRole(), "역할이 일치해야 합니다");
        assertTrue(passwordEncoder.matches("password123", foundUser.getPassword()), 
                   "비밀번호가 암호화되어 저장되어야 합니다");
        
        System.out.println(" DB 연결 확인: 회원가입 데이터가 DB에 정상적으로 저장되었습니다.");
        System.out.println("   - 저장된 사용자 ID: " + foundUser.getUserId());
        System.out.println("   - 저장된 로그인 ID: " + foundUser.getLoginId());
        System.out.println("   - 저장된 이메일: " + foundUser.getEmail());
    }

    @Test
    @DisplayName("DB 연결 확인 - 로그인 후 JWT 토큰 발급 및 검증")
    void testDatabaseConnection_Login() {
        // given - DB에 사용자 저장
        User user = User.builder()
                .loginId("LOGIN001")
                .password(passwordEncoder.encode("password123"))
                .email("login@example.com")
                .name("로그인테스트")
                .role(UserStatus.STUDENT)
                .reservationNum(0)
                .build();
        User savedUser = userRepository.save(user);
        
        System.out.println("DB에 사용자 저장 완료: " + savedUser.getUserId());

        // when - 로그인
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setLoginId("LOGIN001");
        loginRequest.setPassword("password123");
        
        JwtResponse jwtResponse = authService.login(loginRequest);

        // then - JWT 토큰이 발급되었는지 확인
        assertNotNull(jwtResponse.getToken(), "JWT 토큰이 발급되어야 합니다");
        assertFalse(jwtResponse.getToken().isEmpty(), "JWT 토큰이 비어있지 않아야 합니다");
        assertEquals("Bearer", jwtResponse.getType(), "토큰 타입이 Bearer여야 합니다");
        assertEquals("LOGIN001", jwtResponse.getLoginId(), "로그인 ID가 일치해야 합니다");
        assertEquals("STUDENT", jwtResponse.getRole(), "역할이 일치해야 합니다");

        // JWT 토큰 검증
        assertTrue(jwtTokenProvider.validateToken(jwtResponse.getToken()), 
                   "발급된 JWT 토큰이 유효해야 합니다");
        assertEquals("LOGIN001", jwtTokenProvider.getLoginIdFromToken(jwtResponse.getToken()), 
                     "토큰에서 로그인 ID를 추출할 수 있어야 합니다");
        assertEquals("STUDENT", jwtTokenProvider.getRoleFromToken(jwtResponse.getToken()), 
                     "토큰에서 역할을 추출할 수 있어야 합니다");

        System.out.println("DB 연결 확인: 로그인 성공 및 JWT 토큰 발급 완료");
        System.out.println("   - 발급된 토큰: " + jwtResponse.getToken().substring(0, 50) + "...");
        System.out.println("   - 토큰 유효성 검증: 통과");
    }

    @Test
    @DisplayName("DB 연결 확인 - 회원가입 후 즉시 로그인")
    void testDatabaseConnection_RegisterAndLogin() {
        // given
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setLoginId("FLOW001");
        registerRequest.setPassword("password123");
        registerRequest.setEmail("flow@example.com");
        registerRequest.setName("플로우테스트");
        registerRequest.setRole("STUDENT");

        // when 1 - 회원가입
        User savedUser = authService.register(registerRequest);
        Long userId = savedUser.getUserId();
        
        System.out.println("Step 1: 회원가입 완료 (User ID: " + userId + ")");

        // when 2 - 즉시 로그인
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setLoginId("FLOW001");
        loginRequest.setPassword("password123");
        
        JwtResponse jwtResponse = authService.login(loginRequest);

        // then - 전체 플로우 검증
        assertNotNull(userId, "회원가입 시 사용자 ID가 생성되어야 합니다");
        assertNotNull(jwtResponse.getToken(), "로그인 시 JWT 토큰이 발급되어야 합니다");
        
        // DB에서 다시 조회하여 확인
        User foundUser = userRepository.findById(userId)
                .orElseThrow(() -> new AssertionError("회원가입한 사용자를 DB에서 찾을 수 없습니다"));
        
        assertEquals("FLOW001", foundUser.getLoginId(), "DB에 저장된 로그인 ID가 일치해야 합니다");
        assertEquals("flow@example.com", foundUser.getEmail(), "DB에 저장된 이메일이 일치해야 합니다");

        System.out.println("Step 2: 로그인 완료");
        System.out.println("DB 연결 확인: 회원가입 → 로그인 플로우가 정상적으로 동작합니다");
        System.out.println("   - 저장된 사용자 ID: " + foundUser.getUserId());
        System.out.println("   - 발급된 JWT 토큰 길이: " + jwtResponse.getToken().length() + " 문자");
    }

    @Test
    @DisplayName("DB 연결 확인 - 여러 사용자 저장 및 조회")
    void testDatabaseConnection_MultipleUsers() {
        // given
        RegisterRequest request1 = new RegisterRequest();
        request1.setLoginId("MULTI001");
        request1.setPassword("pass1");
        request1.setEmail("multi1@example.com");
        request1.setName("사용자1");
        request1.setRole("STUDENT");

        RegisterRequest request2 = new RegisterRequest();
        request2.setLoginId("MULTI002");
        request2.setPassword("pass2");
        request2.setEmail("multi2@example.com");
        request2.setName("사용자2");
        request2.setRole("ADMIN");

        // when
        User user1 = authService.register(request1);
        User user2 = authService.register(request2);

        // then - DB에 모두 저장되었는지 확인
        long userCount = userRepository.count();
        assertEquals(2, userCount, "DB에 2명의 사용자가 저장되어야 합니다");

        User foundUser1 = userRepository.findByLoginId("MULTI001").orElseThrow();
        User foundUser2 = userRepository.findByLoginId("MULTI002").orElseThrow();

        assertEquals("MULTI001", foundUser1.getLoginId());
        assertEquals(UserStatus.STUDENT, foundUser1.getRole());
        assertEquals("MULTI002", foundUser2.getLoginId());
        assertEquals(UserStatus.ADMIN, foundUser2.getRole());

        System.out.println("DB 연결 확인: 여러 사용자 저장 및 조회 성공");
        System.out.println("   - 저장된 사용자 수: " + userCount);
        System.out.println("   - 사용자1 ID: " + foundUser1.getUserId() + ", 역할: " + foundUser1.getRole());
        System.out.println("   - 사용자2 ID: " + foundUser2.getUserId() + ", 역할: " + foundUser2.getRole());
    }
}

