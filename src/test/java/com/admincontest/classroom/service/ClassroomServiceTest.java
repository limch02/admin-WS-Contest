package com.admincontest.classroom.service;

import com.admincontest.classroom.domain.Classroom;
import com.admincontest.classroom.dto.ClassroomCreateDTO;
import com.admincontest.classroom.dto.ClassroomUpdateDTO;
import com.admincontest.classroom.exception.ClassroomAlreadyExistException;
import com.admincontest.classroom.repository.ClassroomRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
class ClassroomServiceTest {

    @Autowired
    private ClassroomService classroomService;

    @Autowired
    private ClassroomRepository classroomRepository;

    @BeforeEach
    void setUp() {
        classroomRepository.deleteAll();
    }

    @Test
    @DisplayName("강의실 생성 성공")
    void testCreateClassroomSuccess() {
        // given
        ClassroomCreateDTO createDTO = new ClassroomCreateDTO();
        createDTO.setName("공학관 301호");
        createDTO.setLocation("공학관 3층");
        createDTO.setCapacity(30);
        createDTO.setHasWhiteboard(true);
        createDTO.setHasProjector(false);

        // when
        Long classroomId = classroomService.createClassroom(createDTO);

        // then
        assertNotNull(classroomId);
        Classroom savedClassroom = classroomRepository.findById(classroomId).orElseThrow();
        assertEquals("공학관 301호", savedClassroom.getName());
        assertEquals("공학관 3층", savedClassroom.getLocation());
        assertEquals(30, savedClassroom.getCapacity());
        assertTrue(savedClassroom.isHasWhiteboard());
        assertFalse(savedClassroom.isHasProjector());
    }

    @Test
    @DisplayName("강의실 조회 성공 - 존재하는 강의실")
    void testFindClassroomSuccess() {
        // given
        Classroom classroom = Classroom.of("공학관 301호", "공학관 3층", 30, true, false);
        Classroom savedClassroom = classroomRepository.save(classroom);
        Long classroomId = savedClassroom.getId();

        // when
        Classroom foundClassroom = classroomService.findClassroom(classroomId);

        // then
        assertNotNull(foundClassroom);
        assertEquals(classroomId, foundClassroom.getId());
        assertEquals("공학관 301호", foundClassroom.getName());
        assertEquals("공학관 3층", foundClassroom.getLocation());
        assertEquals(30, foundClassroom.getCapacity());
    }

    @Test
    @DisplayName("강의실 조회 실패 - 존재하지 않는 강의실")
    void testFindClassroomNotFound() {
        // given
        Long nonExistentId = 999L;

        // when & then
        ClassroomAlreadyExistException exception = assertThrows(
                ClassroomAlreadyExistException.class,
                () -> classroomService.findClassroom(nonExistentId)
        );
        assertTrue(exception.getMessage().contains("존재하지 않습니다"));
    }

    @Test
    @DisplayName("전체 강의실 조회")
    void testFindAllClassrooms() {
        // given
        Classroom classroom1 = Classroom.of("공학관 301호", "공학관 3층", 30, true, false);
        Classroom classroom2 = Classroom.of("본관 201호", "본관 2층", 50, true, true);
        Classroom classroom3 = Classroom.of("인문관 101호", "인문관 1층", 20, false, true);

        classroomRepository.save(classroom1);
        classroomRepository.save(classroom2);
        classroomRepository.save(classroom3);

        // when
        List<Classroom> classrooms = classroomService.findAllClassrooms();

        // then
        assertEquals(3, classrooms.size());
        assertTrue(classrooms.stream().anyMatch(c -> c.getName().equals("공학관 301호")));
        assertTrue(classrooms.stream().anyMatch(c -> c.getName().equals("본관 201호")));
        assertTrue(classrooms.stream().anyMatch(c -> c.getName().equals("인문관 101호")));
    }

    @Test
    @DisplayName("강의실 전체 조회 - 빈 리스트")
    void testFindAllClassroomsEmpty() {
        // when
        List<Classroom> classrooms = classroomService.findAllClassrooms();

        // then
        assertNotNull(classrooms);
        assertTrue(classrooms.isEmpty());
    }

    @Test
    @DisplayName("강의실 수정 성공")
    void testUpdateClassroomSuccess() {
        // given
        Classroom classroom = Classroom.of("공학관 301호", "공학관 3층", 30, true, false);
        Classroom savedClassroom = classroomRepository.save(classroom);
        Long classroomId = savedClassroom.getId();

        ClassroomUpdateDTO updateDTO = new ClassroomUpdateDTO();
        updateDTO.setName("공학관 302호");
        updateDTO.setLocation("공학관 3층");
        updateDTO.setCapacity(40);
        updateDTO.setHasWhiteboard(true);
        updateDTO.setHasProjector(true);

        // when
        classroomService.updateClassroom(classroomId, updateDTO);

        // then
        Classroom updatedClassroom = classroomRepository.findById(classroomId).orElseThrow();
        assertEquals("공학관 302호", updatedClassroom.getName());
        assertEquals(40, updatedClassroom.getCapacity());
        assertTrue(updatedClassroom.isHasProjector());
    }

    @Test
    @DisplayName("강의실 수정 실패 - 존재하지 않는 강의실")
    void testUpdateClassroomNotFound() {
        // given
        Long nonExistentId = 999L;
        ClassroomUpdateDTO updateDTO = new ClassroomUpdateDTO();
        updateDTO.setName("공학관 302호");
        updateDTO.setLocation("공학관 3층");
        updateDTO.setCapacity(40);
        updateDTO.setHasWhiteboard(true);
        updateDTO.setHasProjector(true);

        // when & then
        assertThrows(
                ClassroomAlreadyExistException.class,
                () -> classroomService.updateClassroom(nonExistentId, updateDTO)
        );
    }

    @Test
    @DisplayName("강의실 삭제 성공")
    void testDeleteClassroomSuccess() {
        // given
        Classroom classroom = Classroom.of("공학관 301호", "공학관 3층", 30, true, false);
        Classroom savedClassroom = classroomRepository.save(classroom);
        Long classroomId = savedClassroom.getId();

        // when
        classroomService.deleteClassroom(classroomId);

        // then
        assertFalse(classroomRepository.findById(classroomId).isPresent());
    }

    @Test
    @DisplayName("강의실 삭제 - 존재하지 않는 강의실 (예외 없이 처리)")
    void testDeleteClassroomNotFound() {
        // given
        Long nonExistentId = 999L;

        // when & then - 존재하지 않는 ID로 삭제해도 예외가 발생하지 않아야 함
        assertDoesNotThrow(() -> classroomService.deleteClassroom(nonExistentId));
    }

    @Test
    @DisplayName("강의실 생성 - 모든 비품이 있는 경우")
    void testCreateClassroomWithAllEquipment() {
        // given
        ClassroomCreateDTO createDTO = new ClassroomCreateDTO();
        createDTO.setName("본관 대강당");
        createDTO.setLocation("본관 1층");
        createDTO.setCapacity(100);
        createDTO.setHasWhiteboard(true);
        createDTO.setHasProjector(true);

        // when
        Long classroomId = classroomService.createClassroom(createDTO);

        // then
        Classroom savedClassroom = classroomRepository.findById(classroomId).orElseThrow();
        assertTrue(savedClassroom.isHasWhiteboard());
        assertTrue(savedClassroom.isHasProjector());
    }

    @Test
    @DisplayName("강의실 생성 - 비품이 없는 경우")
    void testCreateClassroomWithoutEquipment() {
        // given
        ClassroomCreateDTO createDTO = new ClassroomCreateDTO();
        createDTO.setName("간이 강의실");
        createDTO.setLocation("별관 1층");
        createDTO.setCapacity(10);
        createDTO.setHasWhiteboard(false);
        createDTO.setHasProjector(false);

        // when
        Long classroomId = classroomService.createClassroom(createDTO);

        // then
        Classroom savedClassroom = classroomRepository.findById(classroomId).orElseThrow();
        assertFalse(savedClassroom.isHasWhiteboard());
        assertFalse(savedClassroom.isHasProjector());
    }
}

