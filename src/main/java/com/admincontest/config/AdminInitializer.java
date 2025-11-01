package com.admincontest.config;

import com.admincontest.user.domain.User;
import com.admincontest.user.domain.UserStatus;
import com.admincontest.user.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AdminInitializer {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    private static final String ADMIN_LOGIN_ID = "adminid";
    private static final String ADMIN_PASSWORD = "adminpw";
    private static final String ADMIN_EMAIL = "admin@admin.com";
    private static final String ADMIN_NAME = "관리자";


    @PostConstruct
    public void initAdmin() {
        // 관리자 계정이 이미 존재하는지 확인
        if (userRepository.existsByLoginId(ADMIN_LOGIN_ID)) {
            return; // 이미 존재하면 생성하지 않음
        }

        // 관리자 계정 생성
        User admin = User.builder()
                .loginId(ADMIN_LOGIN_ID)
                .password(passwordEncoder.encode(ADMIN_PASSWORD))
                .email(ADMIN_EMAIL)
                .name(ADMIN_NAME)
                .role(UserStatus.ADMIN)
                .reservationNum(0)
                .build();

        userRepository.save(admin);
        System.out.println("=========================================");
        System.out.println("기본 관리자 계정이 생성되었습니다.");
        System.out.println("ID: " + ADMIN_LOGIN_ID);
        System.out.println("PW: " + ADMIN_PASSWORD);
        System.out.println("=========================================");
    }
}

