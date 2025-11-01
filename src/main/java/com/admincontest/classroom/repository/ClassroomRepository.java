package com.admincontest.classroom.repository;

import com.admincontest.classroom.domain.Classroom;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClassroomRepository extends JpaRepository<Classroom, Long> {
}
