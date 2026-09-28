package com.example.complaintbox.dto;

import com.example.complaintbox.enums.Category;
import com.example.complaintbox.enums.Priority;
import com.example.complaintbox.enums.Status;

import java.time.LocalDateTime;

public record ComplaintResponse(
        Long id,
        String userName,
        Category category,
        String title,
        String hostelBlock,
        String floor,
        String roomNumber,
        String landmark,
        String description,
        Priority priority,
        Status status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        boolean overdue
) {
}
