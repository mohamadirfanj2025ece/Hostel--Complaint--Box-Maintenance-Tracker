package com.example.complaintbox.dto;

import com.example.complaintbox.enums.Category;
import com.example.complaintbox.enums.Priority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

// The user does NOT send status or dates. The backend sets them.
public record ComplaintRequest(
        @NotNull(message = "Category is required")
        Category category,

        @NotBlank(message = "Complaint title is required")
        @Size(min = 3, max = 100, message = "Title must be 3 to 100 characters")
        String title,

        @NotBlank(message = "Hostel block is required")
        @Size(max = 50, message = "Hostel block must be at most 50 characters")
        String hostelBlock,

        @NotBlank(message = "Floor is required")
        @Size(max = 20, message = "Floor must be at most 20 characters")
        String floor,

        @NotBlank(message = "Room number is required")
        @Size(max = 10, message = "Room number must be at most 10 characters")
        String roomNumber,

        @Size(max = 100, message = "Landmark must be at most 100 characters")
        String landmark,

        @NotBlank(message = "Description is required")
        @Size(min = 5, max = 500, message = "Description must be 5 to 500 characters")
        String description,

        // Optional. If missing, MEDIUM is used.
        Priority priority
) {
}
