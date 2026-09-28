package com.example.complaintbox.controller;

import com.example.complaintbox.dto.ComplaintRequest;
import com.example.complaintbox.dto.ComplaintResponse;
import com.example.complaintbox.service.ComplaintService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

// USER endpoints. authentication.getName() is the email of the logged-in user.
@RestController
@RequestMapping("/api/complaints")
public class ComplaintController {

    private final ComplaintService complaintService;

    public ComplaintController(ComplaintService complaintService) {
        this.complaintService = complaintService;
    }

    @PostMapping
    public ResponseEntity<ComplaintResponse> createComplaint(@Valid @RequestBody ComplaintRequest request,
                                                              Authentication authentication) {
        ComplaintResponse created = complaintService.createComplaint(authentication.getName(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/my")
    public List<ComplaintResponse> getMyComplaints(Authentication authentication) {
        return complaintService.getMyComplaints(authentication.getName());
    }
}
