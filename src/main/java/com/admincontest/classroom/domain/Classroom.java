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
@Table(name = "classrooms")
public class Classroom {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "classroom_id")
	private Long id;

	@Column(name = "name", nullable = false)
	private String name;

	@Column(name = "location", nullable = false)
	private String location;

	@Column(name = "capacity", nullable = false)
	private int capacity;

	@Column(name = "min_people")
	private Integer minPeople;

	@Column(name = "floor")
	private String floor;

	@Column(name = "has_whiteboard")
	private boolean hasWhiteboard;

	@Column(name = "has_projector")
	private boolean hasProjector;

	@Builder
	private Classroom(String name, String location, int capacity, Integer minPeople, String floor, boolean hasWhiteboard, boolean hasProjector) {
		this.name = name;
		this.location = location;
		this.capacity = capacity;
		this.minPeople = minPeople;
		this.floor = floor;
		this.hasWhiteboard = hasWhiteboard;
		this.hasProjector = hasProjector;
	}

	public static Classroom of(String name, String location, int capacity, Integer minPeople, String floor, boolean hasWhiteboard, boolean hasProjector) {
		return Classroom.builder()
				.name(name)
				.location(location)
				.capacity(capacity)
				.minPeople(minPeople)
				.floor(floor)
				.hasWhiteboard(hasWhiteboard)
				.hasProjector(hasProjector)
				.build();
	}

	public void update(String name, String location, int capacity, Integer minPeople, String floor, boolean hasWhiteboard, boolean hasProjector) {
		this.name = name;
		this.location = location;
		this.capacity = capacity;
		this.minPeople = minPeople;
		this.floor = floor;
		this.hasWhiteboard = hasWhiteboard;
		this.hasProjector = hasProjector;
	}
}
