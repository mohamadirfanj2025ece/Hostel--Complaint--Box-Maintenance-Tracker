package com.example.complaintbox.controller;

import com.example.complaintbox.dto.StudentProfileResponse;
import com.example.complaintbox.dto.StudentProfileRequest;
import com.example.complaintbox.service.StudentService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequestMapping("/api/students")
public class StudentController {

    private final StudentService studentService;

    public StudentController(StudentService studentService) {
        this.studentService = studentService;
    }

    @GetMapping("/me")
    public StudentProfileResponse getMyProfile(Authentication authentication) {
        return studentService.getProfile(authentication.getName());
    }

    @PutMapping("/me/profile")
    public StudentProfileResponse updateMyProfile(Authentication authentication,
                                                  @Valid @RequestBody StudentProfileRequest request) {
        return studentService.updateProfile(authentication.getName(), request);
    }
}