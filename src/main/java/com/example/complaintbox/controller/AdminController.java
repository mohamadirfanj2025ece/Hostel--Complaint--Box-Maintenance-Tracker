package com.example.complaintbox.controller;

import com.example.complaintbox.dto.ComplaintResponse;
import com.example.complaintbox.dto.DashboardResponse;
import com.example.complaintbox.dto.StatusUpdateRequest;
import com.example.complaintbox.service.ComplaintService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

// ADMIN endpoints (SecurityConfig allows only ROLE_ADMIN on /api/admin/**)
@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final ComplaintService complaintService;

    public AdminController(ComplaintService complaintService) {
        this.complaintService = complaintService;
    }

    @GetMapping("/complaints")
    public List<ComplaintResponse> getAllComplaints() {
        return complaintService.getAllComplaints();
    }

    @PutMapping("/complaints/{id}/status")
    public ComplaintResponse updateStatus(@PathVariable Long id,
                                          @Valid @RequestBody StatusUpdateRequest request) {
        return complaintService.updateStatus(id, request.status());
    }

    @GetMapping("/complaints/overdue")
    public List<ComplaintResponse> getOverdueComplaints() {
        return complaintService.getOverdueComplaints();
    }

    @GetMapping("/dashboard")
    public DashboardResponse getDashboard() {
        return complaintService.getDashboard();
    }
}
