package com.admincontest.user.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {
    private String loginId;      // 로그인 ID (학번 등)
    private String password;      // 비밀번호
    private String email;         // 이메일
    private String name;          // 이름
    private String role;          // 역할 ("ADMIN" 또는 "STUDENT")
}