package com.admincontest.classroom.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ClassroomUpdateDTO {
	private String name;
	private String location;
	private int capacity;
	private boolean hasWhiteboard;
	private boolean hasProjector;
}
