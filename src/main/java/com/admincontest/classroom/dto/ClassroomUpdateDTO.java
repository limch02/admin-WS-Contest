package com.admincontest.classroom.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

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
	private LocalDate availableDate;
}
