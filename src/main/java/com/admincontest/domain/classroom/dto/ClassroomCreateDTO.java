package com.admincontest.domain.classroom.dto;

import com.admincontest.domain.classroom.domain.Classroom;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ClassroomCreateDTO {
	private String name;
	private String location;
	private int capacity;
	private boolean hasWhiteboard;
	private boolean hasProjector;

	public Classroom toEntity() {
		return Classroom.of(name, location, capacity, hasWhiteboard, hasProjector);
	}
}
