package com.example.complaintbox.repository;

import com.example.complaintbox.entity.Complaint;
import com.example.complaintbox.entity.User;
import com.example.complaintbox.enums.Status;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface ComplaintRepository extends JpaRepository<Complaint, Long> {

    // Complaints of one user, newest first
    List<Complaint> findByUserOrderByCreatedAtDesc(User user);

    // Not RESOLVED and created before the given time (used for overdue), oldest first
    List<Complaint> findByStatusNotAndCreatedAtBeforeOrderByCreatedAtAsc(Status status, LocalDateTime time);

    long countByStatus(Status status);

    long countByStatusNotAndCreatedAtBefore(Status status, LocalDateTime time);
}
