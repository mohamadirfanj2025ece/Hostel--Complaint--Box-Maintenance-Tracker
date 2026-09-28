package com.example.complaintbox.service;

import com.example.complaintbox.dto.LoginRequest;
import com.example.complaintbox.dto.LoginResponse;
import com.example.complaintbox.dto.AdminRegisterRequest;
import com.example.complaintbox.dto.RegisterRequest;
import com.example.complaintbox.dto.StudentRegisterRequest;
import com.example.complaintbox.dto.UserResponse;
import com.example.complaintbox.entity.User;
import com.example.complaintbox.enums.Role;
import com.example.complaintbox.exception.DuplicateEmailException;
import com.example.complaintbox.exception.InvalidCredentialsException;
import com.example.complaintbox.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public UserResponse register(RegisterRequest request) {
        String email = request.email().trim();
        if (userRepository.existsByEmail(email)) {
            throw new DuplicateEmailException();
        }

        User user = new User();
        user.setName(request.name().trim());
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setRoomNumber(request.roomNumber().trim());
        user.setRole(Role.USER);
        user.setCreatedAt(LocalDateTime.now());

        User saved = userRepository.save(user);
        return new UserResponse(saved.getId(), saved.getName(), saved.getEmail(),
                saved.getRoomNumber(), saved.getRole());
    }

    public UserResponse registerStudent(StudentRegisterRequest request) {
        String email = request.email().trim();
        String registerNumber = request.registerNumber().trim();

        if (userRepository.existsByEmail(email)) {
            throw new DuplicateEmailException();
        }

        if (userRepository.existsByRegisterNumber(registerNumber)) {
            throw new DuplicateEmailException("Register number already exists.");
        }

        User user = new User();
        user.setName(request.name().trim());
        user.setEmail(email);
        user.setRegisterNumber(registerNumber);
        user.setDepartment(request.department().trim());
        user.setHostelBlock(request.hostelBlock().trim());
        user.setFloor(request.floor().trim());
        user.setPhoneNumber(request.phoneNumber().trim());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setRoomNumber(request.roomNumber().trim());
        user.setRole(Role.USER);
        user.setCreatedAt(LocalDateTime.now());

        User saved = userRepository.save(user);
        return new UserResponse(saved.getId(), saved.getName(), saved.getEmail(),
                saved.getRoomNumber(), saved.getRole());
    }

    public synchronized UserResponse registerAdmin(AdminRegisterRequest request) {
        if (userRepository.existsByRole(Role.ADMIN)) {
            throw new InvalidCredentialsException("Admin registration is closed because an admin account already exists.");
        }

        String email = request.email().trim();
        if (userRepository.existsByEmail(email)) {
            throw new DuplicateEmailException();
        }

        User admin = new User();
        admin.setName(request.name().trim());
        admin.setEmail(email);
        admin.setPassword(passwordEncoder.encode(request.password()));
        admin.setRoomNumber("OFFICE");
        admin.setRole(Role.ADMIN);
        admin.setCreatedAt(LocalDateTime.now());

        User saved = userRepository.save(admin);
        return new UserResponse(saved.getId(), saved.getName(), saved.getEmail(),
                saved.getRoomNumber(), saved.getRole());
    }

    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email().trim())
                .orElseThrow(InvalidCredentialsException::new);

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new InvalidCredentialsException();
        }

        return new LoginResponse("Login successful", user.getName(), user.getEmail(), user.getRole());
    }

    public LoginResponse loginStudent(LoginRequest request) {
        return loginForRole(request, Role.USER, "Student login successful");
    }

    public LoginResponse loginAdmin(LoginRequest request) {
        return loginForRole(request, Role.ADMIN, "Admin login successful");
    }

    private LoginResponse loginForRole(LoginRequest request, Role expectedRole, String successMessage) {
        User user = userRepository.findByEmail(request.email().trim())
                .orElseThrow(InvalidCredentialsException::new);

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new InvalidCredentialsException();
        }

        if (!user.getRole().equals(expectedRole)) {
            throw new InvalidCredentialsException("Invalid account type for this login page.");
        }

        return new LoginResponse(successMessage, user.getName(), user.getEmail(), user.getRole());
    }
}
