package com.admincontest.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserManagementDTO {
    private Long userId;
    private String loginId;
    private String name;
    private String email;
    private String role;
    private Integer reservationNum;
    private LocalDateTime createdAt;
}

