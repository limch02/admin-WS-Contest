package com.admincontest.admin.controller;

import com.admincontest.classroom.domain.Classroom;
import com.admincontest.classroom.repository.ClassroomRepository;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
class AdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ClassroomRepository classroomRepository;

    @BeforeEach
    void setUp() {
        classroomRepository.deleteAll();
    }

    @Test
    @DisplayName("강의실 등록 폼 페이지 조회")
    void testCreateClassroomForm() throws Exception {
        // when & then
        mockMvc.perform(get("/admin/classrooms/new"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/create-classroom"))
                .andExpect(model().attributeExists("classroomForm"));
    }

    @Test
    @DisplayName("강의실 목록 페이지 조회")
    void testListClassrooms() throws Exception {
        // given
        Classroom classroom1 = Classroom.of("공학관 301호", "공학관 3층", 30, true, false);
        Classroom classroom2 = Classroom.of("본관 201호", "본관 2층", 50, true, true);
        classroomRepository.save(classroom1);
        classroomRepository.save(classroom2);

        // when & then
        mockMvc.perform(get("/admin/classrooms"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin"))
                .andExpect(model().attributeExists("classrooms"));
    }

    @Test
    @DisplayName("강의실 등록 POST 요청 성공")
    void testCreateClassroomPost() throws Exception {
        // when & then
        mockMvc.perform(post("/admin/classrooms")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("name", "공학관 301호")
                        .param("location", "공학관 3층")
                        .param("capacity", "30")
                        .param("hasWhiteboard", "true")
                        .param("hasProjector", "false"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/classrooms"));

        // DB에 저장되었는지 확인
        assertEquals(1, classroomRepository.count());
        Classroom savedClassroom = classroomRepository.findAll().get(0);
        assertEquals("공학관 301호", savedClassroom.getName());
        assertEquals("공학관 3층", savedClassroom.getLocation());
        assertEquals(30, savedClassroom.getCapacity());
    }

    @Test
    @DisplayName("강의실 등록 POST 요청 - 체크박스가 체크되지 않은 경우")
    void testCreateClassroomPostWithoutEquipment() throws Exception {
        // when & then
        mockMvc.perform(post("/admin/classrooms")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("name", "간이 강의실")
                        .param("location", "별관 1층")
                        .param("capacity", "10")
                        .param("hasWhiteboard", "false")
                        .param("hasProjector", "false"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/classrooms"));

        // DB에서 확인
        Classroom savedClassroom = classroomRepository.findAll().get(0);
        assertFalse(savedClassroom.isHasWhiteboard());
        assertFalse(savedClassroom.isHasProjector());
    }

    @Test
    @DisplayName("강의실 등록 POST 요청 - 모든 비품이 있는 경우")
    void testCreateClassroomPostWithAllEquipment() throws Exception {
        // when & then
        mockMvc.perform(post("/admin/classrooms")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("name", "본관 대강당")
                        .param("location", "본관 1층")
                        .param("capacity", "100")
                        .param("hasWhiteboard", "true")
                        .param("hasProjector", "true"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/classrooms"));

        // DB에서 확인
        Classroom savedClassroom = classroomRepository.findAll().get(0);
        assertTrue(savedClassroom.isHasWhiteboard());
        assertTrue(savedClassroom.isHasProjector());
    }
}

