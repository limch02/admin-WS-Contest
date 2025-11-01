package com.admincontest.user.service;

import com.admincontest.user.domain.User;
import com.admincontest.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String loginId) throws UsernameNotFoundException {
        // DB에서 사용자 조회
        User user = userRepository.findByLoginId(loginId)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + loginId));

        // Spring Security UserDetails 객체 생성
        return new org.springframework.security.core.userdetails.User(
                user.getLoginId(),           // username (로그인 ID)
                user.getPassword(),          // password (암호화된 비밀번호)
                getAuthorities(user)         // authorities (권한)
        );
    }

    // 사용자의 권한을 GrantedAuthority 컬렉션으로 변환
    private Collection<? extends GrantedAuthority> getAuthorities(User user) {
        List<GrantedAuthority> authorities = new ArrayList<>();

        // 역할을 "ROLE_" 접두사와 함께 권한으로 추가
        // 예: ADMIN -> ROLE_ADMIN, STUDENT -> ROLE_STUDENT
        authorities.add(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()));

        return authorities;
    }
}