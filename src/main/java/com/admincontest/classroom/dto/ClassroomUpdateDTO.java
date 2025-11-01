package com.admincontest.classroom.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ClassroomUpdateDTO {
	private String name;
	private String location;
	private int capacity;
	private Integer minPeople;
	private String floor;
	private boolean hasWhiteboard;
	private boolean hasProjector;
}
