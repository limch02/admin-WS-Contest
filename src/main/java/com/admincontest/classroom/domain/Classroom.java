package com.admincontest.classroom.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "CLASSROOMS")
public class Classroom {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "room_id")
	private Long id;

	@Column(name = "name", nullable = false)
	private String name;

	@Column(name = "location", nullable = false)
	private String location;

	@Column(name = "capacity", nullable = false)
	private int capacity;

	@Column(name = "has_whiteboard")
	private boolean hasWhiteboard;

	@Column(name = "has_projector")
	private boolean hasProjector;

	@Column(name = "available_date", nullable = false)
	private LocalDate availableDate;

	@Builder
	private Classroom(String name, String location, int capacity, boolean hasWhiteboard, boolean hasProjector, LocalDate availableDate) {
		this.name = name;
		this.location = location;
		this.capacity = capacity;
		this.hasWhiteboard = hasWhiteboard;
		this.hasProjector = hasProjector;
		this.availableDate = availableDate;
	}

	public static Classroom of(String name, String location, int capacity, boolean hasWhiteboard, boolean hasProjector, LocalDate availableDate) {
		return Classroom.builder()
				.name(name)
				.location(location)
				.capacity(capacity)
				.hasWhiteboard(hasWhiteboard)
				.hasProjector(hasProjector)
				.availableDate(availableDate)
				.build();
	}

	public void update(String name, String location, int capacity, boolean hasWhiteboard, boolean hasProjector, LocalDate availableDate) {
		this.name = name;
		this.location = location;
		this.capacity = capacity;
		this.hasWhiteboard = hasWhiteboard;
		this.hasProjector = hasProjector;
		this.availableDate = availableDate;
	}
}
