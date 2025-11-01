package com.admincontest.admin.controller;

import com.admincontest.classroom.dto.ClassroomCreateDTO;
import com.admincontest.classroom.service.ClassroomService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final ClassroomService classroomService;

    @GetMapping("/classrooms/new")
    public String createClassroomForm(Model model) {
        model.addAttribute("classroomForm", new ClassroomCreateDTO());
        return "admin/create-classroom";
    }

    @GetMapping("/classrooms")
    public String listClassrooms(Model model) {
        model.addAttribute("classrooms", classroomService.findAllClassrooms());
        return "admin";
    }

    @PostMapping("/classrooms")
    public String createClassroom(ClassroomCreateDTO classroomForm) {
        classroomService.createClassroom(classroomForm);
        return "redirect:/admin/classrooms";
    }
}

