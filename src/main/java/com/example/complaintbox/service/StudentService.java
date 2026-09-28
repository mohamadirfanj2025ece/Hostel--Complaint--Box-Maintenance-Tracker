package com.example.complaintbox.service;

import com.example.complaintbox.dto.StudentProfileResponse;
import com.example.complaintbox.dto.StudentProfileRequest;
import com.example.complaintbox.exception.DuplicateEmailException;
import com.example.complaintbox.entity.User;
import com.example.complaintbox.exception.UserNotFoundException;
import com.example.complaintbox.repository.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class StudentService {

    private final UserRepository userRepository;

    public StudentService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public StudentProfileResponse getProfile(String email) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(UserNotFoundException::new);

        return toResponse(user);
    }

    public StudentProfileResponse updateProfile(String email, StudentProfileRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(UserNotFoundException::new);
        String registerNumber = request.registerNumber().trim();
        if (userRepository.existsByRegisterNumberAndIdNot(registerNumber, user.getId())) {
            throw new DuplicateEmailException("Register number already exists.");
        }

        user.setRegisterNumber(registerNumber);
        user.setDepartment(request.department().trim());
        user.setHostelBlock(request.hostelBlock().trim());
        user.setFloor(request.floor().trim());
        user.setRoomNumber(request.roomNumber().trim());
        user.setPhoneNumber(request.phoneNumber().trim());
        return toResponse(userRepository.save(user));
    }

    private StudentProfileResponse toResponse(User user) {
        return new StudentProfileResponse(user.getName(), user.getEmail(), user.getRegisterNumber(),
                user.getDepartment(), user.getHostelBlock(), user.getFloor(), user.getRoomNumber(),
                user.getPhoneNumber());
    }
}