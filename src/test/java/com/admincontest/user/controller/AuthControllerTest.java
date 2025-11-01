package com.admincontest.user.controller;

import com.admincontest.user.dto.JwtResponse;
import com.admincontest.user.dto.LoginRequest;
import com.admincontest.user.dto.RegisterRequest;
import com.admincontest.user.domain.User;
import com.admincontest.user.domain.UserStatus;
import com.admincontest.user.repository.UserRepository;
import com.admincontest.user.service.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthService authService;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("회원가입 성공 테스트")
    void testRegisterSuccess() throws Exception {
        // given
        RegisterRequest request = new RegisterRequest();
        request.setLoginId("STU001");
        request.setPassword("password123");
        request.setEmail("test@example.com");
        request.setName("홍길동");
        request.setRole("STUDENT");

        // when & then
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("회원가입이 완료되었습니다."));

        // DB에 저장되었는지 확인
        assertTrue(userRepository.findByLoginId("STU001").isPresent());
    }

    @Test
    @DisplayName("회원가입 - 중복된 로그인 ID")
    void testRegisterDuplicateLoginId() throws Exception {
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
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("이미 사용 중인 로그인 ID입니다."));
    }

    @Test
    @DisplayName("회원가입 - 중복된 이메일")
    void testRegisterDuplicateEmail() throws Exception {
        // given - 이미 존재하는 사용자
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
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("이미 사용 중인 이메일입니다."));
    }

    @Test
    @DisplayName("로그인 성공 테스트")
    void testLoginSuccess() throws Exception {
        // given - 회원가입된 사용자
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

        // when & then
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.type").value("Bearer"))
                .andExpect(jsonPath("$.loginId").value("STU001"))
                .andExpect(jsonPath("$.role").value("STUDENT"))
                .andReturn();

        // JWT 토큰이 반환되는지 확인
        String responseContent = result.getResponse().getContentAsString();
        JwtResponse jwtResponse = objectMapper.readValue(responseContent, JwtResponse.class);
        assertNotNull(jwtResponse.getToken());
        assertFalse(jwtResponse.getToken().isEmpty());
    }

    @Test
    @DisplayName("로그인 실패 - 잘못된 비밀번호")
    void testLoginWrongPassword() throws Exception {
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
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    @DisplayName("로그인 실패 - 존재하지 않는 사용자")
    void testLoginUserNotFound() throws Exception {
        // given
        LoginRequest request = new LoginRequest();
        request.setLoginId("NOTEXIST");
        request.setPassword("password123");

        // when & then
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    @DisplayName("현재 사용자 정보 조회 - JWT 토큰 유효")
    void testGetCurrentUserWithValidToken() throws Exception {
        // given - 사용자 생성 및 로그인
        User user = User.builder()
                .loginId("STU001")
                .password(passwordEncoder.encode("password123"))
                .email("test@example.com")
                .name("홍길동")
                .role(UserStatus.STUDENT)
                .reservationNum(0)
                .build();
        userRepository.save(user);

        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setLoginId("STU001");
        loginRequest.setPassword("password123");

        // 로그인하여 토큰 획득
        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();

        String loginResponse = loginResult.getResponse().getContentAsString();
        JwtResponse jwtResponse = objectMapper.readValue(loginResponse, JwtResponse.class);
        String token = jwtResponse.getToken();

        // when & then - 토큰으로 현재 사용자 정보 조회
        mockMvc.perform(get("/api/auth/me")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.loginId").value("STU001"));
    }

    @Test
    @DisplayName("현재 사용자 정보 조회 - JWT 토큰 없음")
    void testGetCurrentUserWithoutToken() throws Exception {
        // when & then
        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("회원가입 - ADMIN 역할 테스트")
    void testRegisterAsAdmin() throws Exception {
        // given
        RegisterRequest request = new RegisterRequest();
        request.setLoginId("ADMIN001");
        request.setPassword("password123");
        request.setEmail("admin@example.com");
        request.setName("관리자");
        request.setRole("ADMIN");

        // when & then
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        // DB에서 확인
        User savedUser = userRepository.findByLoginId("ADMIN001").orElseThrow();
        assertEquals(UserStatus.ADMIN, savedUser.getRole());
    }
}

