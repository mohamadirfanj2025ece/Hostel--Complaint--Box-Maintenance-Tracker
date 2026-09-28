package com.example.complaintbox.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AdminRegisterRequest(
        @NotBlank(message = "Name is required")
        @Size(min = 2, max = 50, message = "Name must be 2 to 50 characters")
        String name,

        @NotBlank(message = "Email is required")
        @Email(message = "Email format is invalid")
        @Size(max = 100, message = "Email must be at most 100 characters")
        String email,

        @NotBlank(message = "Password is required")
        @Size(min = 12, max = 72, message = "Password must be 12 to 72 characters")
        String password
) {
}