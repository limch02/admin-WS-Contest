package com.admincontest.user.domain;

import java.time.LocalDateTime;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "Users")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class User {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "user_id")
	private Long userId;

	@Column(name = "ID", length = 9, unique = true, nullable = false)
	private String loginId;

	@Column(name = "password", length = 100, nullable = false)
	private String password;

	@Column(name = "email", length = 50, nullable = false)
	private String email;

	@Column(name = "name", length = 15, nullable = false)
	private String name;

	@Column(name = "created_at")
	private LocalDateTime createdAt;

	@Enumerated(EnumType.STRING)
	@Column(name = "role", length = 10, nullable = false)
	private UserStatus role;

	@Column(name = "reservation_num")
	private Integer reservationNum;

	@PrePersist
	protected void onCreate() {
		if (createdAt == null) {
			createdAt = LocalDateTime.now();
		}
		if (role == null) {
			role = UserStatus.STUDENT;
		}
		if (reservationNum == null) {
			reservationNum = 0;
		}
	}
}