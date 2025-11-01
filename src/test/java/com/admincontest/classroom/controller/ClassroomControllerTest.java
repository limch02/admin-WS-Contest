package com.admincontest.classroom.controller;

import com.admincontest.classroom.domain.Classroom;
import com.admincontest.classroom.dto.ClassroomCreateDTO;
import com.admincontest.classroom.dto.ClassroomUpdateDTO;
import com.admincontest.classroom.repository.ClassroomRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
class ClassroomControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ClassroomRepository classroomRepository;

    @BeforeEach
    void setUp() {
        classroomRepository.deleteAll();
    }

    @Test
    @DisplayName("강의실 생성 API 성공")
    void testCreateClassroomSuccess() throws Exception {
        // given
        ClassroomCreateDTO createDTO = new ClassroomCreateDTO();
        createDTO.setName("공학관 301호");
        createDTO.setLocation("공학관 3층");
        createDTO.setCapacity(30);
        createDTO.setHasWhiteboard(true);
        createDTO.setHasProjector(false);

        // when & then
        mockMvc.perform(post("/api/classrooms")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDTO)))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"));

        // DB에 저장되었는지 확인
        List<Classroom> classrooms = classroomRepository.findAll();
        assertEquals(1, classrooms.size());
        assertEquals("공학관 301호", classrooms.get(0).getName());
    }

    @Test
    @DisplayName("강의실 생성 API - 잘못된 요청 데이터")
    void testCreateClassroomWithInvalidData() throws Exception {
        // given - 필수 필드가 없는 경우 (name이 null)
        ClassroomCreateDTO createDTO = new ClassroomCreateDTO();
        createDTO.setLocation("공학관 3층");
        createDTO.setCapacity(30);

        // when & then
        // JPA validation이나 컨트롤러 validation이 있으면 BadRequest가 될 수 있음
        // 현재는 서비스 계층에서 처리하므로 Created가 될 수도 있음
        // 이름이 null이면 데이터베이스 제약조건 위반으로 인해 에러가 발생할 수 있음
        // 일반적으로 데이터베이스 제약조건 위반 시 5xx 에러가 발생
        mockMvc.perform(post("/api/classrooms")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDTO)))
                .andExpect(status().is5xxServerError());
    }

    @Test
    @DisplayName("강의실 조회 API 성공")
    void testGetClassroomSuccess() throws Exception {
        // given
        Classroom classroom = Classroom.of("공학관 301호", "공학관 3층", 30, true, false);
        Classroom savedClassroom = classroomRepository.save(classroom);
        Long classroomId = savedClassroom.getId();

        // when & then
        mockMvc.perform(get("/api/classrooms/{id}", classroomId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(classroomId))
                .andExpect(jsonPath("$.name").value("공학관 301호"))
                .andExpect(jsonPath("$.location").value("공학관 3층"))
                .andExpect(jsonPath("$.capacity").value(30))
                .andExpect(jsonPath("$.hasWhiteboard").value(true))
                .andExpect(jsonPath("$.hasProjector").value(false));
    }

    @Test
    @DisplayName("강의실 조회 API - 존재하지 않는 강의실")
    void testGetClassroomNotFound() throws Exception {
        // given
        Long nonExistentId = 999L;

        // when & then
        mockMvc.perform(get("/api/classrooms/{id}", nonExistentId))
                .andExpect(status().is5xxServerError());
    }

    @Test
    @DisplayName("전체 강의실 조회 API")
    void testGetAllClassrooms() throws Exception {
        // given
        Classroom classroom1 = Classroom.of("공학관 301호", "공학관 3층", 30, true, false);
        Classroom classroom2 = Classroom.of("본관 201호", "본관 2층", 50, true, true);
        Classroom classroom3 = Classroom.of("인문관 101호", "인문관 1층", 20, false, true);

        classroomRepository.save(classroom1);
        classroomRepository.save(classroom2);
        classroomRepository.save(classroom3);

        // when & then
        mockMvc.perform(get("/api/classrooms"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[0].name").exists())
                .andExpect(jsonPath("$[1].name").exists())
                .andExpect(jsonPath("$[2].name").exists());
    }

    @Test
    @DisplayName("전체 강의실 조회 API - 빈 리스트")
    void testGetAllClassroomsEmpty() throws Exception {
        // when & then
        mockMvc.perform(get("/api/classrooms"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    @DisplayName("강의실 수정 API 성공")
    void testUpdateClassroomSuccess() throws Exception {
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

        // when & then
        mockMvc.perform(put("/api/classrooms/{id}", classroomId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isOk());

        // DB에서 확인
        Classroom updatedClassroom = classroomRepository.findById(classroomId).orElseThrow();
        assertEquals("공학관 302호", updatedClassroom.getName());
        assertEquals(40, updatedClassroom.getCapacity());
        assertTrue(updatedClassroom.isHasProjector());
    }

    @Test
    @DisplayName("강의실 수정 API - 존재하지 않는 강의실")
    void testUpdateClassroomNotFound() throws Exception {
        // given
        Long nonExistentId = 999L;
        ClassroomUpdateDTO updateDTO = new ClassroomUpdateDTO();
        updateDTO.setName("공학관 302호");
        updateDTO.setLocation("공학관 3층");
        updateDTO.setCapacity(40);

        // when & then
        mockMvc.perform(put("/api/classrooms/{id}", nonExistentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().is5xxServerError());
    }

    @Test
    @DisplayName("강의실 삭제 API 성공")
    void testDeleteClassroomSuccess() throws Exception {
        // given
        Classroom classroom = Classroom.of("공학관 301호", "공학관 3층", 30, true, false);
        Classroom savedClassroom = classroomRepository.save(classroom);
        Long classroomId = savedClassroom.getId();

        // when & then
        mockMvc.perform(delete("/api/classrooms/{id}", classroomId))
                .andExpect(status().isNoContent());

        // DB에서 삭제되었는지 확인
        assertFalse(classroomRepository.findById(classroomId).isPresent());
    }

    @Test
    @DisplayName("강의실 삭제 API - 존재하지 않는 강의실")
    void testDeleteClassroomNotFound() throws Exception {
        // given
        Long nonExistentId = 999L;

        // when & then - 존재하지 않는 ID로 삭제해도 NoContent가 반환될 수 있음
        // JPA deleteById는 존재하지 않는 ID에 대해 예외를 던지지 않으므로 NoContent 반환
        mockMvc.perform(delete("/api/classrooms/{id}", nonExistentId))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("강의실 생성 API - 모든 비품이 있는 경우")
    void testCreateClassroomWithAllEquipment() throws Exception {
        // given
        ClassroomCreateDTO createDTO = new ClassroomCreateDTO();
        createDTO.setName("본관 대강당");
        createDTO.setLocation("본관 1층");
        createDTO.setCapacity(100);
        createDTO.setHasWhiteboard(true);
        createDTO.setHasProjector(true);

        // when & then
        mockMvc.perform(post("/api/classrooms")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDTO)))
                .andExpect(status().isCreated());

        Classroom savedClassroom = classroomRepository.findAll().get(0);
        assertTrue(savedClassroom.isHasWhiteboard());
        assertTrue(savedClassroom.isHasProjector());
    }
}

