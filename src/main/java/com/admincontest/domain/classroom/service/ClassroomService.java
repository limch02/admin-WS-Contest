package com.admincontest.domain.classroom.service;

import com.admincontest.domain.classroom.domain.Classroom;
import com.admincontest.domain.classroom.dto.ClassroomCreateDTO;
import com.admincontest.domain.classroom.dto.ClassroomUpdateDTO;
import com.admincontest.domain.classroom.exception.ClassroomAlreadyExistException;
import com.admincontest.domain.classroom.repository.ClassroomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ClassroomService {

    private final ClassroomRepository classroomRepository;

    @Transactional
    public Long createClassroom(ClassroomCreateDTO createDTO) {
        Classroom classroom = createDTO.toEntity();
        Classroom savedClassroom = classroomRepository.save(classroom);
        return savedClassroom.getId();
    }

    public Classroom findClassroom(Long id) {
        return classroomRepository.findById(id)
                .orElseThrow(() -> new ClassroomAlreadyExistException("해당 강의실이 존재하지 않습니다. id=" + id));
    }

    public List<Classroom> findAllClassrooms() {
        return classroomRepository.findAll();
    }

    @Transactional
    public void updateClassroom(Long id, ClassroomUpdateDTO updateDTO) {
        Classroom classroom = findClassroom(id);
        classroom.update(
                updateDTO.getName(),
                updateDTO.getLocation(),
                updateDTO.getCapacity(),
                updateDTO.isHasWhiteboard(),
                updateDTO.isHasProjector()
        );
    }

    @Transactional
    public void deleteClassroom(Long id) {
        classroomRepository.deleteById(id);
    }
}
