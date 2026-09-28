package com.example.complaintbox.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// Note: there is no "role" field, so a user can never choose ADMIN while registering
public record RegisterRequest(
        @NotBlank(message = "Name is required")
        @Size(min = 2, max = 50, message = "Name must be 2 to 50 characters")
        String name,

        @NotBlank(message = "Email is required")
        @Email(message = "Email format is invalid")
        @Size(max = 100, message = "Email must be at most 100 characters")
        String email,

        @NotBlank(message = "Password is required")
        @Size(min = 6, max = 50, message = "Password must be 6 to 50 characters")
        String password,

        @NotBlank(message = "Room number is required")
        @Size(max = 10, message = "Room number must be at most 10 characters")
        String roomNumber
) {
}
