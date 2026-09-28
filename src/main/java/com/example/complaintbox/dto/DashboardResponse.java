package com.example.complaintbox.dto;

public record DashboardResponse(
        long total,
        long open,
        long inProgress,
        long resolved,
        long overdue
) {
}
