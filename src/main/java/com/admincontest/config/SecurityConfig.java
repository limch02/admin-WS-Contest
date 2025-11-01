package com.admincontest.config;

import com.admincontest.security.jwt.JwtAuthenticationFilter;
import com.admincontest.user.service.CustomUserDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import jakarta.servlet.http.HttpServletResponse;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return new ProviderManager(authProvider);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // JWT 사용 시 세션 비활성화
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // JWT 필터 추가 (UsernamePasswordAuthenticationFilter 앞에)
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)

                .authorizeHttpRequests(authorize -> authorize
                        // 공개 접근 허용 (문자열 직접 사용)
                        .requestMatchers(
                                "/api/auth/register",
                                "/api/auth/login",
                                "/",
                                "/css/**",
                                "/js/**",
                                "/images/**",
                                "/style.css",
                                "/register",
                                "/login",
                                "/h2-console/**"
                        ).permitAll()

                        // ADMIN만 접근 가능
                        .requestMatchers("/admin/**", "/api/admin/**").hasRole("ADMIN")

                        // 나머지는 인증 필요
                        .anyRequest().authenticated()
                )

                .csrf(csrf -> csrf
                        .ignoringRequestMatchers("/api/**", "/h2-console/**")
                )

                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                            response.setContentType("application/json;charset=UTF-8");
                            response.getWriter().write("{\"error\":\"인증이 필요합니다.\"}");
                        })
                )

                .headers(headers -> headers.frameOptions().sameOrigin()  // H2 Console 사용을 위해
                );

        return http.build();
    }
}