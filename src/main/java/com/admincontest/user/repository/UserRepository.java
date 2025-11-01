package com.admincontest.user.repository;

import com.admincontest.user.domain.User;
import com.admincontest.user.domain.UserStatus;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByLoginId(String loginId);
    Optional<User> findByEmail(String email);

    boolean existsByLoginId(String loginId);
    boolean existsByEmail(String email);
    
    long countByRole(UserStatus role);
}
