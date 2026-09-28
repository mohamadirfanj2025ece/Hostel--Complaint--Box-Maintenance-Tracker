package com.example.complaintbox.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class ApiInfoController {

    @GetMapping("/info")
    public Map<String, Object> getApiInfo() {
        return Map.of(
                "application", "ComplaintBox",
                "description", "Hostel Maintenance Complaint Tracker API",
                "endpoints", Map.ofEntries(
                    Map.entry("register", "POST /api/auth/register"),
                    Map.entry("studentRegister", "POST /api/auth/student/register"),
                    Map.entry("adminRegister", "POST /api/auth/admin/register"),
                    Map.entry("login", "POST /api/auth/login"),
                    Map.entry("studentLogin", "POST /api/auth/student/login"),
                    Map.entry("adminLogin", "POST /api/auth/admin/login"),
                    Map.entry("createComplaint", "POST /api/complaints"),
                    Map.entry("myComplaints", "GET /api/complaints/my"),
                    Map.entry("myStudentProfile", "GET /api/students/me"),
                    Map.entry("updateStudentProfile", "PUT /api/students/me/profile"),
                    Map.entry("adminDashboard", "GET /api/admin/dashboard")
                )
        );
    }
}