package com.admincontest.user.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class JwtResponse {
    private String token;         // JWT 토큰
    private String type = "Bearer"; // 토큰 타입 (기본값 "Bearer")
    private String loginId;       // 로그인 ID
    private String role;          // 역할 ("ADMIN" 또는 "STUDENT")

    // type에 기본값을 설정하는 생성자
    public JwtResponse(String token, String loginId, String role) {
        this.token = token;
        this.loginId = loginId;
        this.role = role;
        this.type = "Bearer";
    }
}