package com.admincontest.domain.reservation.domain;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table
@NoArgsConstructor
@Getter
public class Reservation {
	@Id
	@Column(name = "reservation_id")
	private Long id;

	@Column(name = "reservation_started_at")
	private LocalDateTime reservation_started_at;

	@Column(name = "reservation_ended_at")
	private LocalDateTime reservation_ended_at;

	@Column(name = "reservation_num")
	private int reservation_num;
}
