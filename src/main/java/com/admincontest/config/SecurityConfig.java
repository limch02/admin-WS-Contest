package com.admincontest.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/**").permitAll()  // API 엔드포인트는 모든 사용자 접근 가능
                .requestMatchers("/", "/reserve", "/room-time", "/reservation-detail", "/static/**").permitAll()  // 예약 관련 페이지 접근 허용
                .anyRequest().authenticated()  // 나머지는 인증 필요
            )
            .csrf(csrf -> csrf
                .ignoringRequestMatchers("/api/**")  // API 엔드포인트는 CSRF 보호 제외
            )
            .headers(headers -> headers.frameOptions().disable());  // 필요시 H2 콘솔 사용을 위해
        
        return http.build();
    }
}

