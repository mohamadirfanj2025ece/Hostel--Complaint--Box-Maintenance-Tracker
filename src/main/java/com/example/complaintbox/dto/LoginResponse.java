package com.example.complaintbox.dto;

import com.example.complaintbox.enums.Role;

public record LoginResponse(
        String message,
        String name,
        String email,
        Role role
) {
}
