package com.admincontest.user.controller;

import com.admincontest.user.dto.LoginRequest;
import com.admincontest.user.dto.RegisterRequest;
import com.admincontest.user.dto.JwtResponse;
import com.admincontest.user.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    // 회원가입
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        try {
            authService.register(request);

            Map<String, String> response = new HashMap<>();
            response.put("message", "회원가입이 완료되었습니다.");
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    // 로그인
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        try {
            JwtResponse jwtResponse = authService.login(request);
            return ResponseEntity.ok(jwtResponse);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "로그인에 실패했습니다. ID와 비밀번호를 확인해주세요.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        }
    }

    // 현재 사용자 정보 조회 (JWT 토큰 필요)
    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser() {
        try {
            // SecurityContext에서 현재 인증된 사용자 정보 가져오기
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            String loginId = authentication.getName();

            // 사용자 정보를 Map으로 구성 (또는 User DTO 사용)
            Map<String, String> userInfo = new HashMap<>();
            userInfo.put("loginId", loginId);
            // 추가 정보는 필요시 UserRepository에서 조회

            return ResponseEntity.ok(userInfo);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }
}