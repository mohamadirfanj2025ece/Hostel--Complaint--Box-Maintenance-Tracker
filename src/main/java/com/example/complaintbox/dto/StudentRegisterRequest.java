package com.example.complaintbox.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record StudentRegisterRequest(
        @NotBlank(message = "Name is required")
        @Size(min = 2, max = 50, message = "Name must be 2 to 50 characters")
        String name,

        @NotBlank(message = "Register number is required")
        @Size(min = 3, max = 50, message = "Register number must be 3 to 50 characters")
        String registerNumber,

        @NotBlank(message = "Email is required")
        @Email(message = "Email format is invalid")
        @Size(max = 100, message = "Email must be at most 100 characters")
        String email,

        @NotBlank(message = "Department is required")
        @Size(max = 100, message = "Department must be at most 100 characters")
        String department,

        @NotBlank(message = "Hostel block is required")
        @Size(max = 50, message = "Hostel block must be at most 50 characters")
        String hostelBlock,

        @NotBlank(message = "Floor is required")
        @Size(max = 20, message = "Floor must be at most 20 characters")
        String floor,

        @NotBlank(message = "Room number is required")
        @Size(max = 10, message = "Room number must be at most 10 characters")
        String roomNumber,

        @NotBlank(message = "Phone number is required")
        @Size(min = 8, max = 20, message = "Phone number must be 8 to 20 characters")
        String phoneNumber,

        @NotBlank(message = "Password is required")
        @Size(min = 6, max = 50, message = "Password must be 6 to 50 characters")
        String password
) {
}