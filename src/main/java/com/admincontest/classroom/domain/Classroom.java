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

	@Builder
	private Classroom(String name, String location, int capacity, boolean hasWhiteboard, boolean hasProjector) {
		this.name = name;
		this.location = location;
		this.capacity = capacity;
		this.hasWhiteboard = hasWhiteboard;
		this.hasProjector = hasProjector;
	}

	public static Classroom of(String name, String location, int capacity, boolean hasWhiteboard, boolean hasProjector) {
		return Classroom.builder()
				.name(name)
				.location(location)
				.capacity(capacity)
				.hasWhiteboard(hasWhiteboard)
				.hasProjector(hasProjector)
				.build();
	}

	public void update(String name, String location, int capacity, boolean hasWhiteboard, boolean hasProjector) {
		this.name = name;
		this.location = location;
		this.capacity = capacity;
		this.hasWhiteboard = hasWhiteboard;
		this.hasProjector = hasProjector;
	}
}
