package com.admincontest.security.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        // 요청 헤더에서 JWT 토큰 추출
        String token = resolveToken(request);
        
        // 디버깅: API 요청인 경우에만 로그 출력
        String requestURI = request.getRequestURI();
        String authHeader = request.getHeader("Authorization");
        
        if (requestURI.startsWith("/api/")) {
            System.out.println("[JWT Filter] ========================================");
            System.out.println("[JWT Filter] Request URI: " + requestURI);
            System.out.println("[JWT Filter] Authorization Header: " + (authHeader != null ? authHeader.substring(0, Math.min(30, authHeader.length())) + "..." : "null"));
            System.out.println("[JWT Filter] Token extracted: " + (token != null));
            
            if (token != null) {
                System.out.println("[JWT Filter] Token length: " + token.length());
                
                try {
                    // 토큰이 있고 유효하면 인증 정보 설정
                    boolean isValid = jwtTokenProvider.validateToken(token);
                    System.out.println("[JWT Filter] Token valid: " + isValid);
                    
                    if (isValid) {
                        // 토큰에서 사용자 정보 추출
                        String loginId = jwtTokenProvider.getLoginIdFromToken(token);
                        String role = jwtTokenProvider.getRoleFromToken(token);
                        
                        System.out.println("[JWT Filter] LoginId: " + loginId);
                        System.out.println("[JWT Filter] Role: " + role);
                        System.out.println("[JWT Filter] Authorities: " + jwtTokenProvider.getAuthoritiesFromToken(token));

                        // Spring Security 인증 객체 생성
                        Authentication authentication = new UsernamePasswordAuthenticationToken(
                                loginId,                    // principal (인증 주체)
                                null,                       // credentials (비밀번호는 필요 없음)
                                jwtTokenProvider.getAuthoritiesFromToken(token)  // 권한
                        );

                        // SecurityContext에 인증 정보 설정
                        SecurityContextHolder.getContext().setAuthentication(authentication);
                        System.out.println("[JWT Filter] ✅ Authentication set successfully");
                    } else {
                        System.out.println("[JWT Filter] ❌ Token validation failed");
                    }
                } catch (Exception e) {
                    System.out.println("[JWT Filter] ❌ Error processing token: " + e.getMessage());
                    e.printStackTrace();
                }
            } else {
                System.out.println("[JWT Filter] ❌ No token in request");
            }
            System.out.println("[JWT Filter] ========================================");
        }

        // 다음 필터로 요청 전달
        filterChain.doFilter(request, response);
    }

    // 요청 헤더에서 JWT 토큰 추출
    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");

        // "Bearer " 접두사가 있으면 제거하고 토큰만 반환
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);  // "Bearer " 길이만큼 제거
        }

        return null;
    }
}