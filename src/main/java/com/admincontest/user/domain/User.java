package com.admincontest.user.domain;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor
@Getter
@Table(name = "users")
public class User {
	@Id
	@Column(name = "user_id")
	private Long id;

	@Column(name = "login-id")
	private String login_id;

	@Column(name = "password")
	private String password;

	@Column(name = "name")
	private String name;

	@Column(name = "created_at")
	private LocalDateTime created_at;

	@Column(name = "role")
	private UserStatus status;

	@Column(name = "reservation_count")
	private int reservation_count;
}
