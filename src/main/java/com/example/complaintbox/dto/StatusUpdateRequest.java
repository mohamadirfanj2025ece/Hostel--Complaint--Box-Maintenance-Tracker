package com.example.complaintbox.dto;

import com.example.complaintbox.enums.Status;
import jakarta.validation.constraints.NotNull;

public record StatusUpdateRequest(
        @NotNull(message = "Status is required (OPEN, IN_PROGRESS or RESOLVED)")
        Status status
) {
}
