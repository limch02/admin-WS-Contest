package com.admincontest.domain.classroom.repository;

import com.admincontest.domain.classroom.domain.Classroom;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClassroomRepository extends JpaRepository<Classroom, Long> {
}
