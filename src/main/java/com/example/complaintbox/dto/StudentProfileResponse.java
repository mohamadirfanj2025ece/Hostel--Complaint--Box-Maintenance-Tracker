package com.example.complaintbox.dto;

public record StudentProfileResponse(
        String name,
        String email,
        String registerNumber,
        String department,
        String hostelBlock,
        String floor,
        String roomNumber,
        String phoneNumber
) {
}