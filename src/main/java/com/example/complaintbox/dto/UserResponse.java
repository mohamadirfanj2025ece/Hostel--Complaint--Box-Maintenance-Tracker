package com.example.complaintbox.dto;

import com.example.complaintbox.enums.Role;

public record UserResponse(
        Long id,
        String name,
        String email,
        String roomNumber,
        Role role
) {
}
