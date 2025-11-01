package com.admincontest.domain.classroom.controller;

import com.admincontest.domain.classroom.domain.Classroom;
import com.admincontest.domain.classroom.dto.ClassroomCreateDTO;
import com.admincontest.domain.classroom.dto.ClassroomUpdateDTO;
import com.admincontest.domain.classroom.service.ClassroomService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/classrooms")
@RequiredArgsConstructor
public class ClassroomController {

    private final ClassroomService classroomService;

    @PostMapping
    public ResponseEntity<Void> createClassroom(@RequestBody ClassroomCreateDTO createDTO) {
        Long classroomId = classroomService.createClassroom(createDTO);
        return ResponseEntity.created(URI.create("/api/classrooms/" + classroomId)).build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Classroom> getClassroom(@PathVariable Long id) {
        Classroom classroom = classroomService.findClassroom(id);
        return ResponseEntity.ok(classroom);
    }

    @GetMapping
    public ResponseEntity<List<Classroom>> getAllClassrooms() {
        List<Classroom> classrooms = classroomService.findAllClassrooms();
        return ResponseEntity.ok(classrooms);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> updateClassroom(@PathVariable Long id, @RequestBody ClassroomUpdateDTO updateDTO) {
        classroomService.updateClassroom(id, updateDTO);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteClassroom(@PathVariable Long id) {
        classroomService.deleteClassroom(id);
        return ResponseEntity.noContent().build();
    }
}
