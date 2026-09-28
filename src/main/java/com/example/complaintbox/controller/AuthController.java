package com.example.complaintbox.controller;

import com.example.complaintbox.dto.LoginRequest;
import com.example.complaintbox.dto.LoginResponse;
import com.example.complaintbox.dto.AdminRegisterRequest;
import com.example.complaintbox.dto.RegisterRequest;
import com.example.complaintbox.dto.StudentRegisterRequest;
import com.example.complaintbox.dto.UserResponse;
import com.example.complaintbox.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(request));
    }

    @PostMapping("/student/register")
    public ResponseEntity<UserResponse> registerStudent(@Valid @RequestBody StudentRegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.registerStudent(request));
    }

    @PostMapping("/admin/register")
    public ResponseEntity<UserResponse> registerAdmin(@Valid @RequestBody AdminRegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.registerAdmin(request));
    }

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @PostMapping("/student/login")
    public LoginResponse studentLogin(@Valid @RequestBody LoginRequest request) {
        return authService.loginStudent(request);
    }

    @PostMapping("/admin/login")
    public LoginResponse adminLogin(@Valid @RequestBody LoginRequest request) {
        return authService.loginAdmin(request);
    }
}
