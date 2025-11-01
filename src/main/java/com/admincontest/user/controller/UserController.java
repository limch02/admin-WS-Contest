package com.admincontest.user.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class UserController {
    
    // 루트 경로로 접근 시 index.html 반환
    @GetMapping("/")
    public String index() {
        return "index";
    }
    
    // 로그인 페이지
    @GetMapping("/login")
    public String login() {
        return "login";
    }
    
    // 회원가입 페이지
    @GetMapping("/register")
    public String register() {
        return "register";
    }
    
    // 예약 페이지
    @GetMapping("/reserve")
    public String reserve() {
        return "reserve";
    }
}
